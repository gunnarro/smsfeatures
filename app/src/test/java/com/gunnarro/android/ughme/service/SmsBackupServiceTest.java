package com.gunnarro.android.ughme.service;

import android.os.Environment;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.model.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.SmsReaderServiceImpl;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmsBackupServiceTest {

    private SmsBackupServiceImpl smsBackupService;

    @Mock
    private SmsReaderServiceImpl smsReaderServiceMock;

    MockedStatic<Environment> mockedStatic = Mockito.mockStatic(Environment.class);

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        mockedStatic
                .when(Environment::getExternalStorageDirectory)
                .thenReturn(new File("src/test/resources"));
        smsBackupService = new SmsBackupServiceImpl(smsReaderServiceMock);
    }

    @After
    public void after() {
        mockedStatic.close();
    }

    @Test
    public void backupSmsInbox() {
        List<Sms> smsInbox = new ArrayList<>();
        Mockito.when(smsReaderServiceMock.getSMSInbox(Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyLong())).thenReturn(smsInbox);
        try {
            smsBackupService.backupSmsInbox();
        } catch (ApplicationException e) {
            Assert.fail();
        }
    }

    @Test
    public void saveSmsBackup() throws IOException {
        List<Sms> smsList = new ArrayList<>();
        Sms sms1 = Sms.builder().timeMs(System.currentTimeMillis()).address("23545454").contactName("gunnar").type("1").body("inbox-message1").count(1).numberOfBlocked(0).numberOfSent(0).numberOfReceived(1).build();
        Sms sms2 = Sms.builder().timeMs(System.currentTimeMillis() + 1000).address("92019486").contactName("per").type("1").body("inbox-message2").count(1).numberOfBlocked(0).numberOfSent(0).numberOfReceived(1).build();
        Sms sms3 = Sms.builder().timeMs(System.currentTimeMillis() + 2000).address("461230").contactName("mom").type("2").body("outbox-message3").count(1).numberOfBlocked(0).numberOfSent(0).numberOfReceived(1).build();
        Sms sms4 = Sms.builder().timeMs(System.currentTimeMillis() + 3000).address("93019486").type("1").body("inbox-message4").count(1).numberOfBlocked(0).numberOfSent(0).numberOfReceived(1).build();

        smsList.add(sms1);
        smsList.add(sms2);
        smsList.add(sms3);
        smsList.add(sms4);
        smsBackupService.saveSmsBackup(smsList);
    }

    @Test
    public void readSmsBackup() {
        List<Sms> list = smsBackupService.getSmsBackup();
        Assert.assertEquals(4, list.size());
        // check descending sort order, i.e newest on top
        Assert.assertNull(list.get(0).getContactName());
        Assert.assertEquals("mom", list.get(1).getContactName());
        Assert.assertEquals("per", list.get(2).getContactName());
        Assert.assertEquals("gunnar", list.get(3).getContactName());
        Assert.assertEquals("1", list.get(2).getType());
    }

    @Test
    public void getSmsBackupMobileNumbersTop10() {
        List<String> list = smsBackupService.getSmsBackupMobileNumbersTop10();
        Assert.assertEquals("[93019486, gunnar, mom, per]", list.toString());
    }

    @Test
    public void matchSmsType() {
        Assert.assertTrue( WordCloudEvent.MESSAGE_TYPE_INBOX.matches(WordCloudEvent.MESSAGE_TYPE_ALL));
        Assert.assertTrue( WordCloudEvent.MESSAGE_TYPE_OUTBOX.matches(WordCloudEvent.MESSAGE_TYPE_ALL));
    }

    @Test
    public void getSmsBackupAsTextByName() {
        String txt = smsBackupService.getSmsBackupAsText("gunnar", WordCloudEvent.MESSAGE_TYPE_INBOX);
        Assert.assertEquals("inbox-message1", txt);

        txt = smsBackupService.getSmsBackupAsText("gunnar", WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        Assert.assertEquals("", txt);

        txt = smsBackupService.getSmsBackupAsText("gunnar", WordCloudEvent.MESSAGE_TYPE_ALL);
        Assert.assertEquals("inbox-message1", txt);
    }

    @Test
    public void getSmsBackupAsTextByMobileNumber() {
        String txt = smsBackupService.getSmsBackupAsText("93019486", WordCloudEvent.MESSAGE_TYPE_INBOX);
        Assert.assertEquals("inbox-message4", txt);

        txt = smsBackupService.getSmsBackupAsText("93019486", WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        Assert.assertEquals("", txt);

        txt = smsBackupService.getSmsBackupAsText("93019486", WordCloudEvent.MESSAGE_TYPE_ALL);
        Assert.assertEquals("inbox-message4", txt);
    }

    @Test
    public void getSmsBackupAsTextForAll() {
        String txt = smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_INBOX);
        Assert.assertEquals("inbox-message4 inbox-message2 inbox-message1", txt);

        txt = smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        Assert.assertEquals("outbox-message3", txt);

        txt = smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_ALL);
        Assert.assertEquals("inbox-message4 outbox-message3 inbox-message2 inbox-message1", txt);
    }

    @Test
    public void saveSmsBackupMetaData() {
        smsBackupService.saveSmsBackupMetaData(new ArrayList<>());
    }

    @Test
    public void readSmsBackupMetaData() {
        SmsBackupInfo info = smsBackupService.readSmsBackupMetaData();
        Assert.assertEquals("BACKED_UP", info.getStatus().name());
    }
}
