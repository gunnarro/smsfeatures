package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.model.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmsBackupServiceTest {

    private SmsBackupService smsBackupService;

    //  @Mock
    //  private Environment environment;

    @Before
    public void init() {
        //   MockitoAnnotations.initMocks(this);
        //   Mockito.when(environment.getExternalStorageDirectory()).thenReturn(new File("src/test/resources"));
        smsBackupService = new SmsBackupService();
        smsBackupService.setAppExtDir(new File("src/test/resources"));
    }

    @Ignore
    @Test
    public void backupSmsInbox() {
        try {
            smsBackupService.backupSmsInbox();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveSmsBackup() throws IOException {
        List<Sms> smsList = new ArrayList<>();
        Sms sms1 = Sms.builder().setDate(System.currentTimeMillis()).setAddress("23545454").setContactName("gunnar").setType("1").setBody("message1").setCount(1).setNumberOfBlocked(0).setNumberOfSent(0).setNumberOfReceived(1).build();
        Sms sms2 = Sms.builder().setDate(System.currentTimeMillis() + 1000).setAddress("92019486").setContactName("per").setType("1").setBody("message2").setCount(1).setNumberOfBlocked(0).setNumberOfSent(0).setNumberOfReceived(1).build();
        Sms sms3 = Sms.builder().setDate(System.currentTimeMillis() + 2000).setAddress("461230").setContactName("mom").setType("1").setBody("message3").setCount(1).setNumberOfBlocked(0).setNumberOfSent(0).setNumberOfReceived(1).build();
        smsList.add(sms1);
        smsList.add(sms2);
        smsList.add(sms3);
        smsBackupService.saveSmsBackup(smsList);
    }

    @Test
    public void readSmsBackup() {
        List<Sms> list = smsBackupService.getSmsBackup();
        Assert.assertEquals(3, list.size());
        // check descending sort order, i.e newest on top
        Assert.assertEquals("mom", list.get(0).getContactName());
        Assert.assertEquals("per", list.get(1).getContactName());
        Assert.assertEquals("gunnar", list.get(2).getContactName());
    }

    @Test
    public void getSmsBackupMobileNumbersTop10() {
        List<String> list = smsBackupService.getSmsBackupMobileNumbersTop10();
        Assert.assertEquals("[gunnar, mom, per]", list.toString());
    }

    @Test
    public void getSmsBackupAsText() {
        String txt = smsBackupService.getSmsBackupAsText(null, WordCloudEvent.WordCloudEventTypeEnum.MESSAGE.name());
        Assert.assertEquals("t", txt);
    }

    @Test
    public void readSmsBackupMetaData() {
        SmsBackupInfo info = smsBackupService.readSmsBackupMetaData();
        Assert.assertEquals("NOT_BACKED_UP", info.getStatus().name());
    }
}
