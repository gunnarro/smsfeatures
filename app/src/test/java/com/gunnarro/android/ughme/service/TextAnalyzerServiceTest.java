package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.junit.Assert;
import org.junit.Test;

public class TextAnalyzerServiceTest {

    @Test
    public void analyzeText() {
        TextAnalyzerServiceImpl w = new TextAnalyzerServiceImpl();
        w.analyzeText("Dette, dette, dette er kun en enhets test, og dette er ingen ting å tule med, spør du meg. antall enhets tester er kun 1", null);
        Assert.assertEquals(16, w.getNumberOfWords().intValue());
        Assert.assertEquals("{dette=4, kun=2, enhets=2, ingen=1, antall=1, test=1, ting=1, tester=1, tule=1, med=1}", w.getWordCountMap(10).toString());
        Assert.assertEquals("{dette=4, kun=2, enhets=2}", w.getWordCountMap(3).toString());
        Assert.assertEquals(4, w.getHighestWordCount());
        Assert.assertEquals(0.64f, w.getHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextEmptyText() {
        TextAnalyzerServiceImpl w = new TextAnalyzerServiceImpl();
        w.analyzeText("", TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP);
        Assert.assertEquals(0, w.getNumberOfWords().intValue());
        Assert.assertEquals(0, w.getWordCountMap(10).size());
        Assert.assertEquals(0, w.getHighestWordCount());
        Assert.assertEquals(0.0f, w.getHighestWordCountPercent(), 0);
    }

    @Test
    public void analyzeTextShortText() {
        TextAnalyzerServiceImpl w = new TextAnalyzerServiceImpl();
        w.analyzeText("Android is always a sweet treat!", TextAnalyzerServiceImpl.DEFAULT_WORD_REGEXP);
        Assert.assertEquals(4, w.getNumberOfWords().intValue());
        Assert.assertEquals(4, w.getWordCountMap(10).size());
        Assert.assertEquals(1, w.getHighestWordCount());
        Assert.assertEquals(0.04f, w.getHighestWordCountPercent(), 0);
    }

}

