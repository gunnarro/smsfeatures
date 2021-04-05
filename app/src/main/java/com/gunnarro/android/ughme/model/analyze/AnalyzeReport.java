package com.gunnarro.android.ughme.model.analyze;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Delombok following annotations:
 * ToString
 * Getter
 * Builder
 */
public class AnalyzeReport {

    private final String createdDateTime;
    // text analyze statistics
    private final int textWordCount;
    private final int textUniqueWordCount;
    private final int textHighestWordCount;
    private final float textHighestWordCountPercent;
    private final long analyzeTimeMs;
    // word cloud statistic
    private int cloudWordCount;
    private int cloudPlacedWordCount;
    private int cloudNotPlacedWordCount;

    private List<ReportItem> reportItems;
    private List<ProfileItem> profileItems;

    AnalyzeReport(int textWordCount, int textUniqueWordCount, int textHighestWordCount, float textHighestWordCountPercent, long analyzeTimeMs, List<ReportItem> reportItems, List<ProfileItem> profileItems) {
        this.createdDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());;
        this.textWordCount = textWordCount;
        this.textUniqueWordCount = textUniqueWordCount;
        this.textHighestWordCount = textHighestWordCount;
        this.textHighestWordCountPercent = textHighestWordCountPercent;
        this.analyzeTimeMs = analyzeTimeMs;
        this.reportItems = reportItems;
        this.profileItems = profileItems;
    }

    public static AnalyzeReportBuilder builder() {
        return new AnalyzeReportBuilder();
    }

    public int getTextWordCount() {
        return this.textWordCount;
    }

    public int getTextUniqueWordCount() {
        return this.textUniqueWordCount;
    }

    public int getTextHighestWordCount() { return textHighestWordCount; }

    public float getTextHighestWordCountPercent() { return textHighestWordCountPercent; }

    public long getAnalyzeTimeMs() {
        return this.analyzeTimeMs;
    }

    public void setCloudWordCount(int cloudWordCount) {
        this.cloudWordCount = cloudWordCount;
    }

    public void setCloudPlacedWordCount(int cloudPlacedWordCount) {
        this.cloudPlacedWordCount = cloudPlacedWordCount;
    }

    public void setCloudNotPlacedWordCount(int cloudNotPlacedWordCount) {
        this.cloudNotPlacedWordCount = cloudNotPlacedWordCount;
    }

    public List<ReportItem> getReportItems() {
        return this.reportItems;
    }

    public List<ProfileItem> getProfileItems() {
        return this.profileItems;
    }


    @NotNull
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AnalyzeReportBuilder{");
        sb.append("numberOfWords=").append(textWordCount);
        sb.append("\n, numberOfUniqueWords=").append(textUniqueWordCount);
        sb.append("\n, highestWordCount=").append(textHighestWordCount);
        sb.append("\n, highestWordCountPercent=").append(textHighestWordCountPercent);
        sb.append("\n, analyzeTimeMs=").append(analyzeTimeMs);
        sb.append('}');
        return sb.toString();
    }

    public static class AnalyzeReportBuilder {
        private int textWordCount;
        private int textUniqueWordCount;
        private int textHighestWordCount;
        private float textHighestWordCountPercent;
        private long analyzeTimeMs;
        private List<ReportItem> reportItems = new ArrayList<>();
        private List<ProfileItem> profileItems = new ArrayList<>();

        AnalyzeReportBuilder() {
        }

        public AnalyzeReportBuilder textWordCount(int textWordCount) {
            this.textWordCount = textWordCount;
            return this;
        }

        public AnalyzeReportBuilder textUniqueWordCount(int textUniqueWordCount) {
            this.textUniqueWordCount = textUniqueWordCount;
            return this;
        }

        public AnalyzeReportBuilder textHighestWordCount(int textHighestWordCount) {
            this.textHighestWordCount = textHighestWordCount;
            return this;
        }

        public AnalyzeReportBuilder textHighestWordCountPercent(float textHighestWordCountPercent) {
            this.textHighestWordCountPercent = textHighestWordCountPercent;
            return this;
        }

        public AnalyzeReportBuilder analyzeTimeMs(long analyzeTimeMs) {
            this.analyzeTimeMs = analyzeTimeMs;
            return this;
        }

        public AnalyzeReportBuilder reportItems(List<ReportItem> reportItems) {
            this.reportItems = reportItems;
            return this;
        }

        public AnalyzeReportBuilder profileItems(List<ProfileItem> profileItems) {
            this.profileItems = profileItems;
            return this;
        }

        public AnalyzeReport build() {
            return new AnalyzeReport(textWordCount, textUniqueWordCount, textHighestWordCount, textHighestWordCountPercent, analyzeTimeMs, reportItems, profileItems);
        }
    }
}
