package com.gunnarro.android.ughme.model.sms;

import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
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
    public void contactNameNotSet() {
        Sms sms = Sms.builder().type(".*").body("this is a sms message").address("+4723545400").count(3).build();
        Assert.assertNull(sms.getContactName());
    }

    @Test
    public void smsFilterByType() {
        List<Sms> smsList = createSmsList();
        List<Sms> inbox = smsList.stream()
                .filter(s -> s.getType().matches(WordCloudEvent.MESSAGE_TYPE_INBOX))
                .collect(Collectors.toCollection(ArrayList::new));
        Assert.assertEquals(9, inbox.size());

        List<Sms> outbox = smsList.stream()
                .filter(s -> s.getType().matches(WordCloudEvent.MESSAGE_TYPE_OUTBOX))
                .collect(Collectors.toCollection(ArrayList::new));
        Assert.assertEquals(2, outbox.size());

        List<Sms> inoutbox = smsList.stream()
                .filter(s -> s.getType().matches(WordCloudEvent.MESSAGE_TYPE_ALL))
                .collect(Collectors.toCollection(ArrayList::new));
        Assert.assertEquals(11, inoutbox.size());
    }

    private void testMap() {
        Map<String, Integer> smsMap = createSmsList().stream()
                .collect(Collectors.groupingBy(Sms::getContactName, Collectors.summingInt(Sms::getCount)));

        List<String> list = Utility.getTop10ValuesFromMap(smsMap);
        list.forEach(s -> System.out.println("Key : " + s));
    }

    private List<Sms> createSmsList() {
        List<Sms> smsList = new ArrayList<>();
        Random rand = new Random();
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 1").address("+4723545400").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 2").address("+4723545401").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 3").address("+4723545402").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 4").address("+4723545403").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 5").address("+4723545404").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 6").address("+4723545405").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 7").address("+4723545406").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 8").address("+4723545407").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_INBOX).body("this is a sms message 9").address("+4723545408").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_OUTBOX).body("this is a sms message 10 ").address("+4723545409").contactName("gunnar").count(rand.nextInt(10)).build());
        smsList.add(Sms.builder().type(WordCloudEvent.MESSAGE_TYPE_OUTBOX).body("this is a sms message 11").address("+4723545410").contactName("gunnar").count(rand.nextInt(10)).build());
        return smsList;
    }
}
