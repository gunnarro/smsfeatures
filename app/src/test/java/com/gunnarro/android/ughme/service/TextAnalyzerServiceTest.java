package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.junit.Assert;
import org.junit.Test;

public class TextAnalyzerServiceTest {

    @Test
    public void analyzeText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText("Dette, dette, dette er kun en enhets test, og dette er ingen ting å tule med, spør du meg. antall enhets tester er kun 1", Sms.INBOX,null, 3, 1);
        Assert.assertEquals(16, report.getTextWordCount());
        Assert.assertEquals("[ReportItem(word=dette, count=4, percentage=50, status=null), ReportItem(word=kun, count=2, percentage=25, status=null), ReportItem(word=enhets, count=2, percentage=25, status=null)]", report.getReportItems().toString());
        Assert.assertEquals(4, report.getTextHighestWordCount());
        Assert.assertEquals(50.0, report.getTextHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextEmptyText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText("", Sms.INBOX, TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP, 10, 1);
        Assert.assertEquals(0, report.getTextWordCount());
        Assert.assertEquals(0, report.getReportItems().size());
        Assert.assertEquals(0, report.getTextHighestWordCount());
        Assert.assertEquals(0.0f, report.getTextHighestWordCountPercent(), 0);

    }

    @Test
    public void analyzeTextShortText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText("Android is always a sweet treat!", Sms.INBOX,TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP,10, 1);
        Assert.assertEquals(4, report.getTextWordCount());
        Assert.assertEquals(4, report.getReportItems().size());
        Assert.assertEquals(1, report.getTextHighestWordCount());
        Assert.assertEquals(25.0f, report.getTextHighestWordCountPercent(), 0);
    }
}

