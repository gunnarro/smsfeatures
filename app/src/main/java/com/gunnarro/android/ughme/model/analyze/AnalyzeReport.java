package com.gunnarro.android.ughme.model.analyze;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeReport {

    private final int numberOfWords;
    private final int numberOfUniqueWords;
    private final long analyzeTimeMs;
    private final List<ReportItem> reportItems;

    public AnalyzeReport(AnalyzeReport.Builder builder) {
        this.numberOfWords = builder.numberOfWords;
        this.numberOfUniqueWords = builder.numberOfUniqueWords;
        this.analyzeTimeMs = builder.analyzeTimeMs;
        this.reportItems = new ArrayList<>();
    }

    public static AnalyzeReport.Builder builder() {
        return new AnalyzeReport.Builder();
    }

    public int getNumberOfWords() {
        return numberOfWords;
    }

    public int getNumberOfUniqueWords() {
        return numberOfUniqueWords;
    }

    public long getAnalyzeTimeMs() {
        return analyzeTimeMs;
    }

    public List<ReportItem> getReportItems() {
        return reportItems;
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AnalyzeReport{");
        sb.append("numberOfWords=").append(numberOfWords);
        sb.append(", numberOfUniqueWords=").append(numberOfUniqueWords);
        sb.append(", analyzeTimeMs=").append(analyzeTimeMs);
        sb.append(", reportItems=").append(reportItems);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder class
     */
    public static class Builder {
        private int numberOfWords;
        private int numberOfUniqueWords;
        private long analyzeTimeMs;
        private List<ReportItem> reportItems;

        private Builder() {
        }

        public AnalyzeReport.Builder numberOfWords(Integer numberOfWords) {
            this.numberOfWords = numberOfWords;
            return this;
        }


        public AnalyzeReport.Builder numberOfUniqueWords(Integer numberOfUniqueWords) {
            this.numberOfUniqueWords = numberOfUniqueWords;
            return this;
        }


        public AnalyzeReport.Builder analyzeTimeMs(Long analyzeTimeMs) {
            this.analyzeTimeMs = analyzeTimeMs;
            return this;
        }

        public AnalyzeReport.Builder reportItems(List<ReportItem> reportItems) {
            this.reportItems = reportItems;
            return this;
        }

        public AnalyzeReport.Builder of(AnalyzeReport analyzeReport) {
            this.numberOfUniqueWords = analyzeReport.numberOfWords;
            this.numberOfUniqueWords = analyzeReport.numberOfUniqueWords;
            this.analyzeTimeMs = analyzeReport.analyzeTimeMs;
            this.reportItems = analyzeReport.reportItems;
            return this;
        }

        public AnalyzeReport build() {
            return new AnalyzeReport(this);
        }
    }
}
