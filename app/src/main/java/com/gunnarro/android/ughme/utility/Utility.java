package com.gunnarro.android.ughme.utility;

import android.util.Log;

import com.gunnarro.android.ughme.model.sms.Sms;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();
    private static final SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String formatTime(Long timeMs) {
        if (timeMs != null && timeMs >= 0) {
            return dateFormat.format(new Date(timeMs));
        }
        return "";
    }


    public static LinkedHashMap<String, Integer> getTop10Values(Map<String, Integer> map) {
        return map.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static List<String> getTop10ValuesFromMap(@NotNull final Map<String, Integer> map) {
        return new ArrayList<>(getTop10Values(map).keySet());
    }


    public static List<Sms> diffLists(@NotNull List<Sms> smsInbox, @NotNull List<Sms> smsBackup) {
        if (smsBackup == null) {
            Log.d(LOG_TAG, "diffLists: no backup, return inbox");
            return smsInbox;
        }
        // take a local copy in order to not delete from original list
        List<Sms> tmpSmsInbox = new ArrayList<>(smsInbox);
        List<Sms> tmpSmsBackup = new ArrayList<>(smsBackup);

        Log.d(LOG_TAG, String.format("diffLists: diff sms inbox (%s) and sms backup (%s)", tmpSmsInbox.size(), tmpSmsBackup.size()));
        List<Sms> diffList = new ArrayList<>();
        // Get already backed sms from inbox,
        List<Sms> unChangedObjects = tmpSmsInbox.stream().filter(tmpSmsBackup::contains).distinct().collect(Collectors.toList());
        // Remove unchanged objects from both lists
        tmpSmsInbox.removeAll(unChangedObjects);
        tmpSmsBackup.removeAll(unChangedObjects);
        if (tmpSmsInbox.equals(tmpSmsBackup)) {
            Log.d(LOG_TAG, "diffLists: sms inbox and sms backup are equal!");
        } else {
            diffList = tmpSmsInbox;
        }
        Log.d(LOG_TAG, String.format("diffLists: Number of sms diff: %s", diffList.size()));
        return diffList;
    }

    public static void mergeList(@NotNull List<Sms> smsBackupList, @NotNull List<Sms> smsNewList) {
        Log.d("Utility", String.format("merge list, backupList: %s, new list: %s ", smsBackupList.size(), smsNewList));
        smsBackupList.addAll(smsNewList);
        smsBackupList.sort((Sms s1, Sms s2) -> s1.getTimeMs().compareTo(s2.getTimeMs()));
        Log.d("Utility", String.format("merged list: %s", smsBackupList.size()));

    }
}