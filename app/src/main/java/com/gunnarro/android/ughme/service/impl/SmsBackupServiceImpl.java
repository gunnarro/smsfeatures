package com.gunnarro.android.ughme.service.impl;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.model.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SmsBackupServiceImpl {

    private static final String LOG_TAG = SmsBackupServiceImpl.class.getSimpleName();

    public static final String SMS_BACKUP_FILE_NAME = "sms-backup.json";
    public static final String SMS_BACKUP_METADATA_FILE_NAME = "sms-backup-metadata.json";

    private final File appExtDir;
    private final SmsReaderServiceImpl smsReaderService;

    @Inject
    public SmsBackupServiceImpl(@NonNull SmsReaderServiceImpl smsReaderService) {
        this.appExtDir = Environment.getExternalStorageDirectory();
        this.smsReaderService = smsReaderService;
    }

    public List<Sms> getSmsInbox(String filterByMobileNumber, Long filterByTimeMs) {
        List<Sms> inbox = smsReaderService.getSMSInbox(false, filterByMobileNumber, filterByTimeMs);
        Log.d(LOG_TAG, String.format("getSmsInbox: filterByNumber: %s, filterByTimeMs: %s, number of sms: %s", filterByMobileNumber, filterByTimeMs, inbox.size()));
        return inbox;
    }

    public List<Sms> getSmsBackup() {
        List<Sms> smsBakupList = new ArrayList<>();
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();
        try {
            File f = getFile(SmsBackupServiceImpl.SMS_BACKUP_FILE_NAME);
            smsBakupList = gson.fromJson(new FileReader(f.getPath()), smsListType);
            if (smsBakupList == null) {
                smsBakupList = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, String.format("sms backup file not found! error: %s", e.getMessage()));
        }
        // sort descending by time
        Comparator<Sms> compareByTimeMs = (Sms s1, Sms s2) -> s1.getTimeMs().compareTo(s2.getTimeMs());
        smsBakupList.sort(compareByTimeMs.reversed());
        return smsBakupList;
    }

    public void backupSmsInbox() {
        try {
            List<Sms> smsBackupList = getSmsBackup();
            Long lastBackupSmsTimeMs = !smsBackupList.isEmpty() ? smsBackupList.get(0).getTimeMs() : null;
            // only get sms that is not already in the backup list
            List<Sms> inbox = getSmsInbox(SmsReaderServiceImpl.SMS_ALL, lastBackupSmsTimeMs);
            List<Sms> newSmsList = Utility.diffLists(inbox, smsBackupList);
            Log.d(LOG_TAG, String.format("backupSmsInbox: diff sms inbox (%s) and sms backup (%s)", inbox.size(), smsBackupList.size()));
            if (!newSmsList.isEmpty()) {
                Utility.mergeList(smsBackupList, newSmsList);
                Log.d(LOG_TAG, String.format("backupSmsInbox: Update backup, new sms: %s, current: %s", newSmsList.size(), smsBackupList.size()));
                saveSmsBackup(smsBackupList);
                Log.d(LOG_TAG, String.format("backupSmsInbox: Saved sms (%s) backup, path: %s", smsBackupList.size(), SMS_BACKUP_FILE_NAME));
            } else {
                Log.d(LOG_TAG, String.format("backupSmsInbox: Backup up to date, %s", SMS_BACKUP_FILE_NAME));
            }
            saveSmsBackupMetaData(smsBackupList);
        } catch (Exception e) {
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public void clearSmsBackupFile() {
        try {
            File backupFile = getFile(SMS_BACKUP_FILE_NAME);
            if (backupFile.exists()) {
                deleteFileContent(backupFile);
                Log.d(LOG_TAG, String.format("clearSmsBackupFile: file: %s", backupFile.getPath()));
            } else {
                Log.e(LOG_TAG, String.format("clearSmsBackupFile: backup file not found! %s", backupFile.getPath()));
            }
            saveSmsBackupMetaData(getSmsBackup());
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("clearSmsBackupFile: Error backup file! %s", e.getLocalizedMessage()));
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public List<String> getSmsBackupMobileNumbersTop10() {
        try {
            List<Sms> smsList = getSmsBackup();
            Map<String, Integer> smsMap = smsList.stream().collect(Collectors.groupingBy(Sms::getContactName, Collectors.summingInt(Sms::getCount)));
            return Utility.getTop10ValuesFromMap(smsMap);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getSmsBackupAsText(@NotNull String contactName, @NonNull String smsType) {
        List<Sms> smsList = getSmsBackup();
        return smsList.stream()
                .filter(s -> s.getContactName().matches(contactName.replace("+", "\\+")) && s.getType().matches(smsType))
                .map(Sms::getBody)
                .collect(Collectors.joining(" "));
    }

    public void saveSmsBackupMetaData(List<Sms> smsBackupList) {
        SmsBackupInfo info = new SmsBackupInfo();
        File smsBackupFile = getFile(SMS_BACKUP_FILE_NAME);
        info.setSmsBackupFileSizeBytes(smsBackupFile.length());
        info.setStorageFreeSpaceBytes(smsBackupFile.getFreeSpace());
        info.setLastBackupTime(smsBackupFile.lastModified());
        info.setSmsBackupFilePath(smsBackupFile.getPath());
        info.setStatus(SmsBackupInfo.BackupStatusEnum.BACKED_UP);
        if (!smsBackupList.isEmpty()) {
            info.setFromDateTime(smsBackupList.get(0).getTimeMs());
            info.setToDateTime(smsBackupList.get(smsBackupList.size() - 1).getTimeMs());
            info.setNumberOfSms(smsBackupList.size());
        }

        File backupMetaFile = getFile(SMS_BACKUP_METADATA_FILE_NAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        try (FileWriter fw = new FileWriter(backupMetaFile, false)) {
            gson.toJson(info, fw);
            fw.flush();
            fw.close();
            Log.d(LOG_TAG, String.format("Saved sms backup info to: %s", backupMetaFile.getPath()));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public SmsBackupInfo readSmsBackupMetaData() {
        try {
            File backUpMetaFile = getFile(SMS_BACKUP_METADATA_FILE_NAME);
            Gson gson = new GsonBuilder().setLenient().create();
            Type smsListType = new TypeToken<SmsBackupInfo>() {
            }.getType();
            SmsBackupInfo info;
            if (backUpMetaFile.exists()) {
                info = gson.fromJson(new FileReader(backUpMetaFile), smsListType);
            } else {
                info = new SmsBackupInfo();
                info.setStatus(SmsBackupInfo.BackupStatusEnum.NOT_BACKED_UP);
                info.setLastBackupTime(null);
                info.setFromDateTime(null);
                info.setToDateTime(null);
            }
            Log.d(LOG_TAG, String.format("readSmsBackupInfo: %s", info));
            return info;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new SmsBackupInfo();
        }
    }

    private File getFile(String fileName) {
        return new File(String.format("%s/%s", appExtDir.getPath(), fileName));
    }

    public void saveSmsBackup(@NotNull List<Sms> smsList) throws IOException {
        File smsBackupFile = getFile(SMS_BACKUP_FILE_NAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        FileWriter fw = new FileWriter(smsBackupFile, false);
        gson.toJson(smsList, fw);
        fw.flush();
        fw.close();
    }

    private void deleteFileContent(@NotNull File file) {
        try {
            new FileWriter(file, false).close();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            throw new ApplicationException(e.getMessage(), e);
        }
    }
}
