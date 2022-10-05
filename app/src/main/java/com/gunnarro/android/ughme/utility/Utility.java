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

    private static String currentUUID;

    private Utility() {
        genNewUUID();
    }

    static {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static void genNewUUID() {
        currentUUID = java.util.UUID.randomUUID().toString();
    }

    public static String buildTag(Class<?> clazz, String tagName) {
        return new StringBuilder(clazz.getSimpleName())
                .append(".").append(tagName)
                .append(", UUID=").append(currentUUID)
                .append(", thread=").append(Thread.currentThread().getName())
                .toString();
    }


    public static String formatTime(Long timeMs) {
        if (timeMs != null && timeMs >= 0) {
            return dateFormat.format(new Date(timeMs));
        }
        return "";
    }

    /**
     * @param map the map of words to be sorted
     * @return return a sorted map by value, i.e most frequent word at top
     */
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

    public static List<Sms> diffLists(@NotNull List<Sms> smsInbox, @NotNull List<Sms> smsBackedUp) {
        // take a local copy in order to not delete from original list
        List<Sms> tmpSmsInbox = new ArrayList<>(smsInbox);
        List<Sms> tmpSmsBackedUp = new ArrayList<>(smsBackedUp);
        // Get already backed sms from inbox,
        List<Sms> unChangedObjects = tmpSmsInbox.stream().filter(tmpSmsBackedUp::contains).distinct().collect(Collectors.toList());
        Log.d(LOG_TAG + ".diffLists", String.format("diff sms inbox: %s and sms backed up: %s, unchanged sms: %s", tmpSmsInbox.size(), tmpSmsBackedUp.size(), unChangedObjects.size()));
        // Remove unchanged objects from both lists
        tmpSmsInbox.removeAll(unChangedObjects);
        Log.d(LOG_TAG + ".diffLists", String.format("Number of sms diff: %s", tmpSmsInbox.size()));
        return tmpSmsInbox;
    }

    public static void mergeList(@NotNull List<Sms> smsBackedupList, @NotNull List<Sms> smsNewList) {
        Log.d("Utility.mergeList", String.format("backed up list: %s, new list: %s ", smsBackedupList.size(), smsNewList.size()));
        smsBackedupList.addAll(diffLists(smsNewList, smsBackedupList));
        Log.d("Utility.mergeList", String.format("merged: %s", smsBackedupList.size()));
    }
}