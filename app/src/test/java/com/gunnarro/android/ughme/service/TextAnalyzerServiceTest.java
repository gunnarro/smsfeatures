package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.model.analyze.AnalyzeReport;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.junit.Assert;
import org.junit.Test;

public class TextAnalyzerServiceTest {

    @Test
    public void analyzeText() {
        TextAnalyzerServiceImpl w = new TextAnalyzerServiceImpl();
        AnalyzeReport report = w.analyzeText("Dette, dette, dette er kun en enhets test, og dette er ingen ting å tule med, spør du meg. antall enhets tester er kun 1", null);
        Assert.assertEquals(16, report.getTextWordCount());
        Assert.assertEquals("{dette=4, kun=2, enhets=2, ingen=1, antall=1, test=1, ting=1, tester=1, tule=1, med=1}", w.getWordCountMap(10).toString());
        Assert.assertEquals("{dette=4, kun=2, enhets=2}", w.getWordCountMap(3).toString());
        Assert.assertEquals(4, report.getTextHighestWordCount());
        Assert.assertEquals(25.0, report.getTextHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextEmptyText() {
        TextAnalyzerServiceImpl w = new TextAnalyzerServiceImpl();
        AnalyzeReport report = w.analyzeText("", TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP);
        Assert.assertEquals(0, report.getTextWordCount());
        Assert.assertEquals(0, w.getWordCountMap(10).size());
        Assert.assertEquals(0, report.getTextHighestWordCount());
        Assert.assertEquals(0.0f, report.getTextHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextShortText() {
        TextAnalyzerServiceImpl w = new TextAnalyzerServiceImpl();
        AnalyzeReport report = w.analyzeText("Android is always a sweet treat!", TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP);
        Assert.assertEquals(4, report.getTextWordCount());
        Assert.assertEquals(4, w.getWordCountMap(10).size());
        Assert.assertEquals(1, report.getTextHighestWordCount());
        Assert.assertEquals(25.0f, report.getTextHighestWordCountPercent(), 0);
    }

}

