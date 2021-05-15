package com.gunnarro.android.ughme;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestData {

    public static List<Sms> createSmsList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File("src/test/resources/sms-backup-loadtest.json"), new TypeReference<List<Sms>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createAllSmsAsText() {
        return createSmsList().stream()
                .filter(s -> s.getType().matches(WordCloudEvent.MESSAGE_TYPE_ALL) && s.getName().matches(WordCloudFragment.ALL_SEARCH))
                .map(Sms::getBody)
                .collect(Collectors.joining(" "));
    }

    public static List<Sms> generateSms(int numberOfSms) {
        List<Sms> smsList = new ArrayList<>();
        for (int i = 0; i < numberOfSms; i++) {
            smsList.add(Sms.builder().type("1").address(Integer.toString(i * 100)).body("sms msg " + 1).build());
        }
        return smsList;
    }
}


