package com.gunnarro.android.ughme.model.report;

import org.junit.Assert;
import org.junit.Test;

public class AnalyzeReportTest {

    @Test
    public void buildReport() {
        AnalyzeReport report = AnalyzeReport.builder().analyzeTimeMs(234).textHighestWordCount(3456).textHighestWordCount(23).textUniqueWordCount(12).textWordCount(234567).build();
        Assert.assertNotNull(report.getProfileItems());
        Assert.assertNotNull(report.getReportItems());

        report.getProfileItems().add(ProfileItem.builder().className("BuildWordCloudTask").method("analyzeText").executionTime(234).build());
        Assert.assertEquals(1, report.getProfileItems().size());
    }
}
