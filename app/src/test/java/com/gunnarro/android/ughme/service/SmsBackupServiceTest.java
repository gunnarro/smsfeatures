package com.gunnarro.android.ughme.service;

import android.content.Context;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.report.AnalyzeReport;
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
import java.util.Map;

public class SmsBackupServiceTest {

    private SmsBackupServiceImpl smsBackupService;

    @Mock
    private SmsReaderServiceImpl smsReaderServiceMock;

    @Mock
    private Context applicationContextMock;

    MockedStatic<Context> mockedStatic = Mockito.mockStatic(Context.class);

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        mockedStatic
                .when(applicationContextMock::getFilesDir)
                .thenReturn(new File("src/test/resources"));
        smsBackupService = new SmsBackupServiceImpl(smsReaderServiceMock, applicationContextMock);
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
            smsBackupService.backupSmsInbox(true);
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
        Sms sms4 = Sms.builder().timeMs(System.currentTimeMillis() + 3000).address("+4793019486").type("1").body("inbox-message4").count(1).numberOfBlocked(0).numberOfSent(0).numberOfReceived(1).build();

        smsList.add(sms1);
        smsList.add(sms2);
        smsList.add(sms3);
        smsList.add(sms4);
        smsBackupService.saveSmsBackup(smsList, false);
    }

    @Test
    public void readSmsBackup() {
        List<Sms> list = smsBackupService.getSmsBackup(true);
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
        Assert.assertEquals("[gunnar, mom, per, +4793019486]", list.toString());
    }

    @Test
    public void matchSmsType() {
        Assert.assertTrue(WordCloudEvent.MESSAGE_TYPE_INBOX.matches(WordCloudEvent.MESSAGE_TYPE_ALL));
        Assert.assertTrue(WordCloudEvent.MESSAGE_TYPE_OUTBOX.matches(WordCloudEvent.MESSAGE_TYPE_ALL));
    }

    @Test
    public void getSmsBackupAsTextByName() {
        Map<String, String> map = smsBackupService.getSmsBackupAsText("gunnar", WordCloudEvent.MESSAGE_TYPE_INBOX);
        Assert.assertEquals("inbox-message1", map.get(WordCloudEvent.MESSAGE_TYPE_INBOX));

        map = smsBackupService.getSmsBackupAsText("gunnar", WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        Assert.assertEquals("", map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX));

        map = smsBackupService.getSmsBackupAsText("gunnar", WordCloudEvent.MESSAGE_TYPE_ALL);
        Assert.assertEquals("inbox-message1", map.get(WordCloudEvent.MESSAGE_TYPE_INBOX));
        Assert.assertEquals("", map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX));
    }

    @Test
    public void getSmsBackupAsTextByMobileNumber() {
        Map<String, String> map = smsBackupService.getSmsBackupAsText("+4793019486", WordCloudEvent.MESSAGE_TYPE_INBOX);
        Assert.assertEquals("inbox-message4", map.get(WordCloudEvent.MESSAGE_TYPE_INBOX));

        map = smsBackupService.getSmsBackupAsText("93019486", WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        Assert.assertEquals("", map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX));

        map = smsBackupService.getSmsBackupAsText("93019486", WordCloudEvent.MESSAGE_TYPE_ALL);
        Assert.assertEquals("", map.get(WordCloudEvent.MESSAGE_TYPE_INBOX));
        Assert.assertEquals("", map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX));
    }

    @Test
    public void getSmsBackupAsTextForAll() {
        Map<String, String> map = smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_INBOX);
        Assert.assertEquals("inbox-message1 inbox-message2 inbox-message4", map.get(WordCloudEvent.MESSAGE_TYPE_INBOX));

        map = smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        Assert.assertEquals("outbox-message3", map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX));

        map = smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_ALL);
        Assert.assertEquals("inbox-message1 inbox-message2 inbox-message4", map.get(WordCloudEvent.MESSAGE_TYPE_INBOX));
        Assert.assertEquals("outbox-message3", map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX));
    }

    @Test
    public void saveSmsBackupMetaData() {
        smsBackupService.saveSmsBackupMetaData(new ArrayList<>());
    }

    @Test
    public void readSmsBackupMetaData() {
        SmsBackupInfo info = smsBackupService.readSmsBackupMetaData();
        Assert.assertEquals("BACKED_UP", info.getStatus().name());
        Assert.assertEquals("sms-backup.json", info.getBackupFileName());
        Assert.assertEquals("src/test/resource", info.getBackupFilePath());
        Assert.assertEquals(0, info.getNumberOfSms().intValue());
    }

    @Test
    public void saveAndReadAnalyzeReport() {
        smsBackupService.saveAnalyseReport(AnalyzeReport.builder().analyzeTimeMs(2035).profileItems(new ArrayList<>()).reportItems(new ArrayList<>()).build());
        AnalyzeReport report = smsBackupService.readAnalyzeReport();
        Assert.assertEquals(2035, report.getAnalyzeTimeMs());
    }
}
