package com.gunnarro.android.ughme.service.impl;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.report.ProfileItem;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.model.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final String WORD_SEPARATOR = " ";


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
    public List<Sms> getSmsBackup(boolean isSorted) {
        long startTime = System.currentTimeMillis();
        List<Sms> smsBackupList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            File smsBackupFile = getFile(SmsBackupServiceImpl.SMS_BACKUP_FILE_NAME);
            smsBackupList = mapper.readValue(smsBackupFile, new TypeReference<List<Sms>>() {
            });
            if (smsBackupList == null) {
                smsBackupList = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.d(Utility.buildTag(getClass(), "getSmsBackup"), String.format("sms backup file not found! error: %s", e.getMessage()));
        }
        // sort descending by time
        if (isSorted) {
            Comparator<Sms> compareByTimeMs = (Sms s1, Sms s2) -> s1.getTimeMs().compareTo(s2.getTimeMs());
            smsBackupList.sort(compareByTimeMs.reversed());
        }
        Log.i(Utility.buildTag(getClass(), "getSmsBackup"), String.format("exeTime= %s ms", (System.currentTimeMillis() - startTime)));
        return smsBackupList;
    }

    public void backupSmsInbox(boolean isSaveExternal) {
        try {
            List<ProfileItem> profileItems = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            List<Sms> smsBackupList = getSmsBackup(true);
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
                saveSmsBackup(smsBackupList, isSaveExternal);
                Log.d(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("Saved sms (%s) backup, path: %s", smsBackupList.size(), SMS_BACKUP_FILE_NAME));
            } else {
                Log.d(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("Backup up to date, %s", SMS_BACKUP_FILE_NAME));
            }
            saveSmsBackupMetaData(smsBackupList);
            profile(profileItems);
            Log.i(Utility.buildTag(getClass(), "backupSmsInbox"), String.format("exeTime= %s ms", (System.currentTimeMillis() - startTime)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public void deleteSmsBackupFile() {
        try {
            File backupFile = getFile(SMS_BACKUP_FILE_NAME);
            deleteFile(backupFile);
            saveSmsBackupMetaData(getSmsBackup(true));
        } catch (Exception e) {
            Log.e(Utility.buildTag(getClass(), "clearSmsBackupFile"), String.format("Error backup file! %s", e.getLocalizedMessage()));
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public List<String> getSmsBackupMobileNumbersTop10() {
        try {
            List<Sms> smsList = getSmsBackup(false);
            Map<String, Integer> smsMap = smsList.stream().collect(Collectors.groupingBy(Sms::getName, Collectors.summingInt(Sms::getCount)));
            return Utility.getTop10ValuesFromMap(smsMap);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * @param filterBy holds either mobile number and contact name
     * @param smsType  can be 1 = INBOX, 2 = OUTBOX or (.*) = All
     * @return all sms messages mergred into a plain text string
     */
    public Map<String,String> getSmsBackupAsText(@NotNull String filterBy, @NonNull String smsType) {
        List<Sms> smsList = getSmsBackup(false);
        String regexp = filterBy.replace("+", "\\+");
        Log.d(Utility.buildTag(getClass(), "getSmsBackupAtText"), String.format("filterBy=%s, smsType=%s, numberOfSms=%s", regexp, smsType, smsList.size()));

        if (smsType.equals(WordCloudEvent.MESSAGE_TYPE_ALL)) {
            String inbox = smsList.stream()
                    .filter(s -> s.getType().equals(WordCloudEvent.MESSAGE_TYPE_INBOX) && s.getName().matches(regexp))
                    .map(Sms::getBody)
                    .collect(Collectors.joining(WORD_SEPARATOR));

            String outbox = smsList.stream()
                    .filter(s -> s.getType().equals(WordCloudEvent.MESSAGE_TYPE_OUTBOX) && s.getName().matches(regexp))
                    .map(Sms::getBody)
                    .collect(Collectors.joining(WORD_SEPARATOR));

            return Map.of(WordCloudEvent.MESSAGE_TYPE_INBOX, inbox, WordCloudEvent.MESSAGE_TYPE_OUTBOX, outbox);
        } else if (smsType.equals(WordCloudEvent.MESSAGE_TYPE_INBOX)) {
            String inbox = smsList.stream()
                    .filter(s -> s.getType().equals(WordCloudEvent.MESSAGE_TYPE_INBOX) && s.getName().matches(regexp))
                    .map(Sms::getBody)
                    .collect(Collectors.joining(WORD_SEPARATOR));
            return Map.of(WordCloudEvent.MESSAGE_TYPE_INBOX, inbox);
        } else if (smsType.equals(WordCloudEvent.MESSAGE_TYPE_OUTBOX)) {
            String outbox = smsList.stream()
                    .filter(s -> s.getType().equals(WordCloudEvent.MESSAGE_TYPE_OUTBOX) && s.getName().matches(regexp))
                    .map(Sms::getBody)
                    .collect(Collectors.joining(WORD_SEPARATOR));
            return Map.of(WordCloudEvent.MESSAGE_TYPE_OUTBOX, outbox);
        }
        return Map.of();
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
        try {
            File backupMetaFile = getFile(SMS_BACKUP_METADATA_FILE_NAME);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(backupMetaFile, info);
            Log.d(Utility.buildTag(getClass(), "saveSmsBackupMetaData"), String.format("Saved sms backup info to: %s", backupMetaFile.getPath()));
        } catch (Exception e) {
            Log.e(Utility.buildTag(getClass(), "saveSmsBackupMetaData"), e.getMessage());
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public SmsBackupInfo readSmsBackupMetaData() {
        try {
            File backUpMetaFile = getFile(SMS_BACKUP_METADATA_FILE_NAME);
            SmsBackupInfo info;
            if (backUpMetaFile.exists() && backUpMetaFile.length() > 0) {
                ObjectMapper mapper = new ObjectMapper();
                info = mapper.readValue(backUpMetaFile, SmsBackupInfo.class);
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
        } catch (Exception e) {
            e.printStackTrace();
            return SmsBackupInfo.builder().build();
        }
    }


    public void saveSmsBackup(@NotNull List<Sms> smsList, boolean isSaveExternal) throws IOException {
        File smsBackupFile = getFile(SMS_BACKUP_FILE_NAME);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(smsBackupFile, smsList);

        if (isSaveExternal) {
            // before android 10
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            mapper.writeValue(new File(folder, SMS_BACKUP_FILE_NAME), smsList);
            Log.d(Utility.buildTag(getClass(), "saveSmsBackup"), String.format("Saved sms (%s) backup, path: %s/%s", smsList.size(), folder.getPath(), SMS_BACKUP_FILE_NAME));
        }
    }

    public void saveAnalyseReport(@NotNull AnalyzeReport analyzeReport) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(getFile(SMS_ANALYZE_REPORT_FILE_NAME), analyzeReport);
        } catch (IOException ioe) {
            Log.e("saveAnalyseReport failed", ioe.getMessage());
        }
    }

    /**
     *
     * @param smsBakupFile external sms backup to be imported
     */
    public List<Sms> inportSmsBackup(java.io.InputStream smsBakupFile) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Sms> smsBackupList = smsBackupList = mapper.readValue(smsBakupFile, new TypeReference<List<Sms>>() {
            });
            if (smsBackupList == null) {
                smsBackupList = new ArrayList<>();
            }
            return smsBackupList;
        } catch (Exception e) {
            Log.d(Utility.buildTag(getClass(), "inportSmsBackup"), String.format("Failed import sms backup! error: %s", e.getMessage()));
            throw new ApplicationException("Failed import sms backup file!", e);
        }
    }

    public AnalyzeReport readAnalyzeReport() {
        try {
            File backUpMetaFile = getFile(SMS_ANALYZE_REPORT_FILE_NAME);
            ObjectMapper mapper = new ObjectMapper();
            AnalyzeReport report = mapper.readValue(backUpMetaFile, AnalyzeReport.class);
            Log.d(Utility.buildTag(getClass(), "readSmsAnalyzeReport"), String.format("%s", report));
            return report;
        } catch (Exception ioe) {
            Log.e(Utility.buildTag(getClass(), "readAnalyzeReport"), ioe.getMessage());
            return null;
        }
    }

    public void profile(List<ProfileItem> profileItems) {
        AnalyzeReport report = readAnalyzeReport();
        if (report != null && report.getProfileItems() != null) {
            profileItems.forEach(p -> report.getProfileItems().add(p));
            saveAnalyseReport(report);
        }
    }

    public void saveWordMap(@NotNull Map<String, Integer> wordMap) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(getFile(SMS_WORD_MAP_FILE_NAME), wordMap);
        } catch (IOException ioe) {
            Log.e(Utility.buildTag(getClass(), "saveWordMap"), ioe.getMessage());
        }
    }

    /**
     * If not exist an new empty file is created
     *
     * @param fileName
     * @return
     */
    private File getFile(String fileName) {
        try {
            File file = new File(String.format("%s/%s", appExternalDir.getPath(), fileName));
            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (Exception e) {
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    private void deleteFile(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            Log.e(Utility.buildTag(getClass(), "deleteFile"), e.getMessage());
            throw new ApplicationException(e.getMessage(), e);
        }
    }
}
