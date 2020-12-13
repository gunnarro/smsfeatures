package com.gunnarro.android.ughme;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.ui.fragment.SmsFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String formatTime(long timeMs) {
        return dateFormat.format(new Date(timeMs));
    }

    public static FilenameFilter getJsonFileNameFilter() {
        return (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.endsWith(".json");
        };
    }

    public static List<Sms> getSmsBackup(String filePath) {
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();
        try {
            File f = new File(filePath);
            return gson.fromJson(new FileReader(f.getPath()), smsListType);
        } catch (
                FileNotFoundException e) {
            Log.d(LOG_TAG, String.format("sms backup file not found! error: %s", e.getMessage()));
            return new ArrayList<>();
        }
    }

    public static LinkedHashMap<String, Integer> getTop10Values(Map<String, Integer> map) {
         return map.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                 .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static List<String> getTop10ValuesFromMap(final Map<String, Integer> map) {
        LinkedHashMap<String, Integer> linkedMap = getTop10Values(map);
        linkedMap.forEach((k,v) -> System.out.println("Key : " + k + ", value: " + v));
        return linkedMap.keySet().stream().collect(Collectors.toList());
    }

}
