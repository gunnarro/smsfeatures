package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.TestData;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.junit.Assert;
import org.junit.Test;

public class TextAnalyzerServiceLoadTest {

    @Test
    public void analyzeText() {
        TextAnalyzerServiceImpl service = new TextAnalyzerServiceImpl();
        service.analyzeText(TestData.createAllSmsAsText(), "\\b\\w{3,}");
        Assert.assertEquals(85345, service.getNumberOfWords().intValue());
        Assert.assertEquals(3779, service.getHighestWordCount());
        Assert.assertEquals(3225187.5, service.getHighestWordCountPercent(), 0);
        Assert.assertEquals(7848, service.getReport(true).getNumberOfUniqueWords());
        Assert.assertEquals("{jeg=3779, det=3527, ikke=2735, til=1740, deg=1722, har=1704, med=1509, meg=1204, for=1160, kan=1111}", service.getWordCountMap(10).toString());
    }
}

