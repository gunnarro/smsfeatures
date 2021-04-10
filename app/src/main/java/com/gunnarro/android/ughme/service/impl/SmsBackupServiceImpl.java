package com.gunnarro.android.ughme.service.impl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.report.ProfileItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SmsBackupServiceImpl {

    public static final String SMS_BACKUP_FILE_NAME = "sms-backup.json";
    public static final String SMS_BACKUP_METADATA_FILE_NAME = "sms-backup-metadata.json";
    public static final String SMS_ANALYZE_REPORT_FILE_NAME = "sms-analyze-report.json";
    public static final String SMS_WORD_MAP_FILE_NAME = "sms-word-map.json";


    // Files meant for your app's use only
    private final File appExternalDir;
    private final SmsReaderServiceImpl smsReaderService;

    @Inject
    public SmsBackupServiceImpl(@NonNull SmsReaderServiceImpl smsReaderService, @ApplicationContext Context context) {
        this.appExternalDir = context.getFilesDir();
        this.smsReaderService = smsReaderService;
        Log.d(Utility.buildTag(getClass(), ""), "app file dir: " + appExternalDir.getPath());
    }

    @NotNull
    public List<Sms> getSmsInbox(String filterByMobileNumber, Long filterByTimeMs) {
        List<Sms> inbox = smsReaderService.getSMSInbox(false, filterByMobileNumber, filterByTimeMs);
        Log.d(Utility.buildTag(getClass(), "getSmsInbox"), String.format("filterByNumber: %s, filterByTimeMs: %s, number of sms: %s", filterByMobileNumber, filterByTimeMs, inbox.size()));
        return inbox;
    }

    @NotNull
    public List<Sms> getSmsBackup() {
        long startTime = System.currentTimeMillis();
        List<Sms> smsBackupList = new ArrayList<>();
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();
        try {
            File smsBackupFile = getFile(SmsBackupServiceImpl.SMS_BACKUP_FILE_NAME);
            smsBackupList = gson.fromJson(new FileReader(smsBackupFile.getPath()), smsListType);
            if (smsBackupList == null) {
                smsBackupList = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            Log.d(Utility.buildTag(getClass(), "getSmsBackup"), String.format("sms backup file not found! error: %s", e.getMessage()));
        }
        // sort descending by time
        Comparator<Sms> compareByTimeMs = (Sms s1, Sms s2) -> s1.getTimeMs().compareTo(s2.getTimeMs());
        smsBackupList.sort(compareByTimeMs.reversed());
        Log.i(Utility.buildTag(getClass(),"getSmsBackup"), String.format("exeTime= %s ms", (System.currentTimeMillis() - startTime)));
        return smsBackupList;
    }

    public void backupSmsInbox() {
        try {
            List<ProfileItem> profileItems = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            List<Sms> smsBackupList = getSmsBackup();
            profileItems.add(ProfileItem.builder().className("SmsBackupServiceImpl").method("getSmsBackup").executionTime(System.currentTimeMillis() - startTime).build());
            Long lastBackupSmsTimeMs = !smsBackupList.isEmpty() ? smsBackupList.get(0).getTimeMs() : null;
            // only get sms that is not already in the backup list
            List<Sms> inbox = getSmsInbox(SmsReaderServiceImpl.SMS_ALL, lastBackupSmsTimeMs);
            profileItems.add(ProfileItem.builder().className("SmsBackupServiceImpl").method("getSmsInbox").executionTime(System.currentTimeMillis() - startTime).build());

            List<Sms> newSmsList = Utility.diffLists(inbox, smsBackupList);
            profileItems.add(ProfileItem.builder().className("SmsBackupServiceImpl").method("diffLists").executionTime(System.currentTimeMillis() - startTime).build());

            Log.d(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("diff sms inbox (%s) and sms backup (%s)", inbox.size(), smsBackupList.size()));
            if (!newSmsList.isEmpty()) {
                Utility.mergeList(smsBackupList, newSmsList);
                Log.d(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("Update backup, new sms: %s, current: %s", newSmsList.size(), smsBackupList.size()));
                saveSmsBackup(smsBackupList);
                Log.d(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("Saved sms (%s) backup, path: %s", smsBackupList.size(), SMS_BACKUP_FILE_NAME));
            } else {
                Log.d(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("Backup up to date, %s", SMS_BACKUP_FILE_NAME));
            }
            saveSmsBackupMetaData(smsBackupList);
            profile(profileItems);
            Log.i(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("exeTime= %s ms", (System.currentTimeMillis() - startTime)));
        } catch (Exception e) {
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public void clearSmsBackupFile() {
        try {
            File backupFile = getFile(SMS_BACKUP_FILE_NAME);
            if (backupFile.exists()) {
                deleteFileContent(backupFile);
                Log.d(Utility.buildTag(getClass(), "clearSmsBackupFile"), String.format("file: %s", backupFile.getPath()));
            } else {
                Log.e(Utility.buildTag(getClass(), "clearSmsBackupFile"), String.format("backup file not found! %s", backupFile.getPath()));
            }
            saveSmsBackupMetaData(getSmsBackup());
        } catch (Exception e) {
            Log.e(Utility.buildTag(getClass(), "clearSmsBackupFile"), String.format("Error backup file! %s", e.getLocalizedMessage()));
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public List<String> getSmsBackupMobileNumbersTop10() {
        try {
            List<Sms> smsList = getSmsBackup();
            Map<String, Integer> smsMap = smsList.stream().collect(Collectors.groupingBy(Sms::getName, Collectors.summingInt(Sms::getCount)));
            return Utility.getTop10ValuesFromMap(smsMap);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * @param filterBy holds either mobile number and contact name
     * @param smsType     can be 1 = INBOX, 2 = OUTBOX or (.*) = All
     * @return all sms messages bundled as a plain text string
     */
    public String getSmsBackupAsText(@NotNull String filterBy, @NonNull String smsType) {
        List<Sms> smsList = getSmsBackup();
        String regexp = filterBy.replace("+", "\\+");
        Log.d(Utility.buildTag(getClass(), "getSmsBackupAtText"), String.format("filterBy=%s, smsType=%s, numberOfSms=%s", regexp, smsType, smsList.size()));
        return smsList.stream()
                .filter(s -> s.getType().matches(smsType) && s.getName().matches(regexp))
                .map(Sms::getBody)
                .collect(Collectors.joining(" "));
    }

    public void saveSmsBackupMetaData(List<Sms> smsBackupList) {
        File smsBackupFile = getFile(SMS_BACKUP_FILE_NAME);
        SmsBackupInfo info = SmsBackupInfo.builder()
                .smsBackupFileSizeBytes(smsBackupFile.length())
                .storageFreeSpaceBytes(smsBackupFile.getFreeSpace())
                .lastBackupTime(smsBackupFile.lastModified())
                .smsBackupFilePath(smsBackupFile.getPath())
                .status(SmsBackupInfo.BackupStatusEnum.BACKED_UP)
                .build();

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
            Log.d(Utility.buildTag(getClass(), "saveSmsBackupMetaData"), String.format("Saved sms backup info to: %s", backupMetaFile.getPath()));
        } catch (Exception e) {
            Log.e(Utility.buildTag(getClass(), "saveSmsBackupMetaData"), e.getMessage());
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
                info = SmsBackupInfo.builder()
                        .status(SmsBackupInfo.BackupStatusEnum.NOT_BACKED_UP)
                        .lastBackupTime(null)
                        .fromDateTime(null)
                        .toDateTime(null)
                        .build();
            }
            Log.d(Utility.buildTag(getClass(), "readSmsBackupInfo"), String.format("%s", info));
            return info;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return SmsBackupInfo.builder().build();
        }
    }

    private File getFile(String fileName) {
        return new File(String.format("%s/%s", appExternalDir.getPath(), fileName));
    }

    public void saveSmsBackup(@NotNull List<Sms> smsList) throws IOException {
        File smsBackupFile = getFile(SMS_BACKUP_FILE_NAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        FileWriter fw = new FileWriter(smsBackupFile, false);
        gson.toJson(smsList, fw);
        fw.flush();
        fw.close();
    }

    public void saveAnalyseReport(@NotNull AnalyzeReport analyzeReport) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            FileWriter fw = new FileWriter(getFile(SMS_ANALYZE_REPORT_FILE_NAME), false);
            gson.toJson(analyzeReport, fw);
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            Log.e("saveAnalyseReport failed", ioe.getMessage());
        }
    }

    public AnalyzeReport readAnalyzeReport() {
        try {
            AnalyzeReport report = AnalyzeReport.builder().build();
            File backUpMetaFile = getFile(SMS_ANALYZE_REPORT_FILE_NAME);
            Gson gson = new GsonBuilder().setLenient().create();
            Type smsListType = new TypeToken<AnalyzeReport>() {
            }.getType();
            if (backUpMetaFile.exists()) {
                report = gson.fromJson(new FileReader(backUpMetaFile), smsListType);
            }
            Log.d(Utility.buildTag(getClass(), "readSmsAnalyzeReport"), String.format("%s", report));
            return report;
        } catch (FileNotFoundException ioe) {
            Log.e(Utility.buildTag(getClass(), "readAnalyzeReport"), ioe.getMessage());
            return null;
        }
    }

    public void profile(List<ProfileItem> profileItems ) {
            AnalyzeReport report = readAnalyzeReport();
            if (report != null && report.getProfileItems() != null) {
                profileItems.forEach(p -> report.getProfileItems().add(p));
                saveAnalyseReport(report);
            }
    }

    public void saveWordMap(@NotNull Map<String, Integer> wordMap) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            FileWriter fw = new FileWriter(getFile(SMS_WORD_MAP_FILE_NAME), false);
            gson.toJson(wordMap, fw);
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            Log.e(Utility.buildTag(getClass(), "saveWordMap"), ioe.getMessage());
        }
    }

    public Map<String, Integer> readWordMap() {
        try {
            File wordMapFile = getFile(SMS_WORD_MAP_FILE_NAME);
            Gson gson = new GsonBuilder().setLenient().create();
            Type mapType = new TypeToken<Map<String, Integer>>() {
            }.getType();
            Map<String, Integer> wordMap = new HashMap<>();
            if (wordMapFile.exists()) {
                wordMap = gson.fromJson(new FileReader(wordMapFile), mapType);
            }
            Log.d(Utility.buildTag(getClass(), "readWordMap"), String.format("%s", wordMap));
            return wordMap;
        } catch (FileNotFoundException ioe) {
            Log.e(Utility.buildTag(getClass(), "readWordMap"), ioe.getMessage());
            return readWordMap();
        }
    }

    private void deleteFileContent(@NotNull File file) {
        try {
            new FileWriter(file, false).close();
        } catch (IOException e) {
            Log.e(Utility.buildTag(getClass(), "deleteFileContent"), e.getMessage());
            throw new ApplicationException(e.getMessage(), e);
        }
    }
}
