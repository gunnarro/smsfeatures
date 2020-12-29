package com.gunnarro.android.ughme.utility;

import com.gunnarro.android.ughme.model.sms.Sms;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilityTest {

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
    public void diffList() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backupList = new ArrayList<>();
        backupList.add(Sms.builder().setAddress("235454").setDate(System.currentTimeMillis()).build());
        smsList.add(Sms.builder().setAddress("235454").setDate(System.currentTimeMillis() + 1000).build());
        Assert.assertEquals(1, Utility.diffLists(backupList, smsList).size());
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
        List<Sms> backupList = new ArrayList<>();
        backupList.add(Sms.builder().setAddress("235454").setDate(System.currentTimeMillis()).build());
        smsList.add(Sms.builder().setAddress("235454").setDate(System.currentTimeMillis() + 1000).build());
        Utility.mergeList(backupList, smsList);
        Assert.assertEquals(2, backupList.size());
    }

    @Test
    public void mergeListEqualList() {
        List<Sms> smsList = new ArrayList<>();
        List<Sms> backupList = new ArrayList<>();
        Sms sms = Sms.builder().setAddress("235454").setDate(System.currentTimeMillis()).build();
        backupList.add(sms);
        smsList.add(sms);
        Utility.mergeList(backupList, smsList);
        Assert.assertEquals(2, backupList.size());
    }

}
