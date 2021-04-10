package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.junit.Assert;
import org.junit.Test;

public class TextAnalyzerServiceTest {

    @Test
    public void analyzeText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText("Dette, dette, dette er kun en enhets test, og dette er ingen ting å tule med, spør du meg. antall enhets tester er kun 1", null, 3);
        Assert.assertEquals(16, report.getTextWordCount());
        Assert.assertEquals("{dette=4, kun=2, enhets=2}", report.getWordMap().toString());
        Assert.assertEquals(4, report.getTextHighestWordCount());
        Assert.assertEquals(50.0, report.getTextHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextEmptyText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText("", TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP, 10);
        Assert.assertEquals(0, report.getTextWordCount());
        Assert.assertEquals(0, report.getWordMap().size());
        Assert.assertEquals(0, report.getTextHighestWordCount());
        Assert.assertEquals(0.0f, report.getTextHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextShortText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        AnalyzeReport report = service.analyzeText("Android is always a sweet treat!", TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP, 10);
        Assert.assertEquals(4, report.getTextWordCount());
        Assert.assertEquals(4, report.getWordMap().size());
        Assert.assertEquals(1, report.getTextHighestWordCount());
        Assert.assertEquals(25.0f, report.getTextHighestWordCountPercent(), 0);
    }
}

