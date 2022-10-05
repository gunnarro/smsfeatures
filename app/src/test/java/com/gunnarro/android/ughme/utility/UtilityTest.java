package com.gunnarro.android.ughme.utility;

import com.gunnarro.android.ughme.model.sms.Sms;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UtilityTest {

    @Test
    public void formatTime() {
        Assert.assertTrue(!Utility.formatTime(System.currentTimeMillis()).isEmpty());
    }

    @Test
    public void random() {
        Random r = new Random();
        r.setSeed(1);
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println("---");
        r.setSeed(2);
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println("---");
        r.setSeed(1);
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println("---");
        r.setSeed(2);
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println("---");
        r.setSeed(123456789);
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println("---");
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
        System.out.println(r.nextInt(256));
    }

    @Test
    public void getTop10ValuesFromMap() {
        List<String> list = Utility.getTop10ValuesFromMap(new HashMap<>());
        Assert.assertEquals("[]", list.toString());
    }

    @Test
    public void getTop10Values() {
        Map<String, Integer> map = Utility.getTop10Values(new HashMap<>());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void diffListNotEqual() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backedupList = new ArrayList<>();
        long time = System.currentTimeMillis();
        backedupList.add(Sms.builder().address("235454").timeMs(time).build());
        smsList.add(Sms.builder().address("235454").timeMs(time).build());
        smsList.add(Sms.builder().address("461230").timeMs(System.currentTimeMillis() + 1000).build());
        Assert.assertEquals(1, Utility.diffLists(smsList, backedupList).size());
    }

    @Test
    public void diffListEqual() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backedupList = new ArrayList<>();
        long time = System.currentTimeMillis();
        backedupList.add(Sms.builder().address("235454").timeMs(time).build());
        smsList.add(Sms.builder().address("235454").timeMs(time).build());
        Assert.assertEquals(0, Utility.diffLists(smsList, backedupList).size());
    }

    @Test
    public void diffListLargeAllEquals() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backedupList = new ArrayList<>();
        for (int i = 0; i<10000; i++) {
            long time = System.currentTimeMillis();
            backedupList.add(Sms.builder().address("0047 23" + i).timeMs(time).build());
            smsList.add(Sms.builder().address("0047 23" + i).timeMs(time).build());
        }
        Assert.assertEquals(0, Utility.diffLists(smsList, backedupList).size());
    }

    @Test
    public void diffListLargeNotEquals() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backedupList = new ArrayList<>();
        for (int i = 0; i<10000; i++) {
            backedupList.add(Sms.builder().address("0047 23" + i).timeMs(System.currentTimeMillis()).build());
            smsList.add(Sms.builder().address("0047 46" + i).timeMs(System.currentTimeMillis()).build());
        }
        // add one equal
        long time = System.currentTimeMillis();
        backedupList.add(Sms.builder().address("235454").timeMs(time).build());
        smsList.add(Sms.builder().address("235454").timeMs(time).build());
        Assert.assertEquals(10000, Utility.diffLists(smsList, backedupList).size());
    }

    @Test
    public void mergeListEmptyLists() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backupList = new ArrayList<>();
        Utility.mergeList(backupList, smsList);
        Assert.assertEquals("[]", backupList.toString());
    }

    @Test
    public void mergeListDifferentList() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backedupList = new ArrayList<>();
        backedupList.add(Sms.builder().address("235454").timeMs(System.currentTimeMillis()).build());
        smsList.add(Sms.builder().address("235454").timeMs(System.currentTimeMillis() + 1000).build());
        // add one equal
        long time = System.currentTimeMillis();
        backedupList.add(Sms.builder().address("235454").timeMs(time).build());
        smsList.add(Sms.builder().address("235454").timeMs(time).build());
        Utility.mergeList(backedupList, smsList);
        Assert.assertEquals(3, backedupList.size());
    }

    @Test
    public void mergeListEqualList() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backupList = new ArrayList<>();
        Sms sms = Sms.builder().address("235454").timeMs(System.currentTimeMillis()).build();
        backupList.add(sms);
        smsList.add(sms);
        Utility.mergeList(backupList, smsList);
        Assert.assertEquals(1, backupList.size());
    }


    private List<Sms> generateSmsList(int numberOfSms) {
        Random rand = new Random();
        List<Sms> list = new ArrayList<>();
        for (int i = 0; i < numberOfSms; i++) {
            list.add(Sms.builder().address(mobileNumbers[rand.nextInt(mobileNumbers.length - 1)])
                    .body(generateSentence(rand.nextInt(100)))
                    .build());
        }
        return list;
    }


    String[] mobileNumbers = {"23545400", "23545411", "23545422", "23545433", "23545444", "23545455", "23545466", "23545466", "23545477", "235454588", "23545499"};
    List<String> words;

    /**
     * Setningsledd
     * verbal - verb
     * subjekt - subjektet
     * objekt - direkte og/eller indirekte, for eks: indirekte: gunnar, direkte: en ball
     * adverbial - adverbet
     * predikativ -  beskriver subjektet eller objektet i en setning
     */
    private String generateSentence(int numberOfWords) {
        Random random = new Random();
        StringBuilder sentence = new StringBuilder();
        for (int i = 0; i < numberOfWords; i++) {
            sentence.append(words.get(random.nextInt(words.size() - 1))).append(" ");
        }
        //Log.d("unit-test", sentence.toString());
        return sentence.toString();
    }
}
