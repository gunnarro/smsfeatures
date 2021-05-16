package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.TestData;
import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TextAnalyzerServiceLoadTest {

    @Test
    public void analyzeText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText(TestData.createAllSmsAsText(), Sms.INBOX, "\\b\\w{3,}", 10);
        Assert.assertEquals(85345, report.getTextWordCount());
        Assert.assertEquals(3779, report.getTextHighestWordCount());
        Assert.assertEquals(4.427910327911377, report.getTextHighestWordCountPercent(), 0);
        Assert.assertEquals(7848, report.getTextUniqueWordCount());
        Assert.assertEquals("{jeg=3779, det=3527, ikke=2735, til=1740, deg=1722, har=1704, med=1509, meg=1204, for=1160, kan=1111}", report.getReportItems());
    }
}

