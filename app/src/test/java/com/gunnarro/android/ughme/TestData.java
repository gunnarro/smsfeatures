package com.gunnarro.android.ughme;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TestData {

    public static List<Sms> createSmsList() {
        Gson gson = new Gson();
        Type smsListType = new TypeToken<List<Sms>>() {}.getType();
        try (JsonReader reader = new JsonReader(new FileReader("src/test/resources/sms-backup-loadtest.json"))) {
            return gson.fromJson(reader, smsListType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String createAllSmsAsText() {
        return createSmsList().stream()
                .filter(s -> s.getType().matches(WordCloudEvent.MESSAGE_TYPE_ALL) && s.getName().matches(WordCloudFragment.ALL_SEARCH))
                .map(Sms::getBody)
                .collect(Collectors.joining(" "));
    }
}


