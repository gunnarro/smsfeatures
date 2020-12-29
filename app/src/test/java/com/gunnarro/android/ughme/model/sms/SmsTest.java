package com.gunnarro.android.ughme.model.sms;

import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.utility.Utility;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SmsTest {

    @Test
    public void contactName() {
        String value = "+4723545400";
        Sms sms = Sms.builder().setType(".*").setBody("this is a sms message").setAddress("+4723545400").setCount(3).build();
        Assert.assertEquals(Boolean.TRUE, sms.getContactName().matches(value.replace("+", "\\+")));
    }

    @Test
    public void matchSmsType() {
        String value = "+4723545400";
        Sms sms = Sms.builder().setType(".*").setBody("this is a sms message").setAddress("+4723545400").setCount(3).build();
        Assert.assertEquals(Boolean.TRUE, sms.getContactName().matches(value.replace("+", "\\+")));
        List<Sms> smsList = new ArrayList<>();
        smsList.add(sms);

        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545400").setContactName("gunnar").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545401").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545402").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545403").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545404").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545405").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545406").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545407").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545408").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545409").setCount(rand.nextInt(10)).build());
            smsList.add(Sms.builder().setType(".*").setBody("this is a sms message" + i).setAddress("+4723545410").setCount(rand.nextInt(10)).build());
        }

        Map<String, Integer> smsMap = smsList.stream()
                .collect(Collectors.groupingBy(Sms::getContactName
                        , Collectors.summingInt(Sms::getCount)));

        List<String> list = Utility.getTop10ValuesFromMap(smsMap);
        list.forEach(s -> System.out.println("Key : " + s));
    }
}
