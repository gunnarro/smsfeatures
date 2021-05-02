package com.gunnarro.android.ughme.service;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.gunnarro.android.ughme.TestData;
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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class SmsBackupServiceLoadTest {

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
        List<Sms> smsInbox = TestData.createSmsList();
        Mockito.when(smsReaderServiceMock.getSMSInbox(Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyLong())).thenReturn(smsInbox);
        try {
            smsBackupService.backupSmsInbox(true);
        } catch (ApplicationException e) {
            Assert.fail();
        }
    }

    @Test
    public void getSmsBackupAsText() {
         String allSmsTxt =  smsBackupService.getSmsBackupAsText(WordCloudFragment.ALL_SEARCH, WordCloudEvent.MESSAGE_TYPE_ALL);
         Assert.assertEquals(636576, allSmsTxt.length());

    }

    @Test
    public void getSmsBackupMobileNumbersTop10() {
        Assert.assertEquals(10, smsBackupService.getSmsBackupMobileNumbersTop10().size());
    }
}
