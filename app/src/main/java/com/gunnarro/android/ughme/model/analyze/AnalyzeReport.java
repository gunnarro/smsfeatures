package com.gunnarro.android.ughme.model.analyze;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Delombok follwoing annotations:
 * ToString
 * Getter
 * Builder
 */
public class AnalyzeReport {

    private final int numberOfWords;
    private final int numberOfUniqueWords;
    private final long analyzeTimeMs;
    private final List<ReportItem> reportItems;

    AnalyzeReport(int numberOfWords, int numberOfUniqueWords, long analyzeTimeMs, List<ReportItem> reportItems) {
        this.numberOfWords = numberOfWords;
        this.numberOfUniqueWords = numberOfUniqueWords;
        this.analyzeTimeMs = analyzeTimeMs;
        this.reportItems = reportItems;
    }

    public static AnalyzeReportBuilder builder() {
        return new AnalyzeReportBuilder();
    }

    public int getNumberOfWords() {
        return this.numberOfWords;
    }

    public int getNumberOfUniqueWords() {
        return this.numberOfUniqueWords;
    }

    public long getAnalyzeTimeMs() {
        return this.analyzeTimeMs;
    }

    public List<ReportItem> getReportItems() {
        return this.reportItems;
    }

    @NotNull
    public String toString() {
        return "AnalyzeReport(numberOfWords=" + this.getNumberOfWords() + ", numberOfUniqueWords=" + this.getNumberOfUniqueWords() + ", analyzeTimeMs=" + this.getAnalyzeTimeMs() + ", reportItems=" + this.getReportItems() + ")";
    }

    public static class AnalyzeReportBuilder {
        private int numberOfWords;
        private int numberOfUniqueWords;
        private long analyzeTimeMs;
        private List<ReportItem> reportItems;

        AnalyzeReportBuilder() {
        }

        public AnalyzeReportBuilder numberOfWords(int numberOfWords) {
            this.numberOfWords = numberOfWords;
            return this;
        }

        public AnalyzeReportBuilder numberOfUniqueWords(int numberOfUniqueWords) {
            this.numberOfUniqueWords = numberOfUniqueWords;
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

        public AnalyzeReport build() {
            return new AnalyzeReport(numberOfWords, numberOfUniqueWords, analyzeTimeMs, reportItems);
        }
    }
}
