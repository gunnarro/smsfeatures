package com.gunnarro.android.ughme.model.report;

import org.jetbrains.annotations.NotNull;

/**
 * Delombok following annotations
 * <p>
 * ToString
 * Getter
 * Builder
 */
public class ReportItem {

    private final String word;
    private final Integer count;
    private final Integer percentage;
    // can be PLACED and NOT_PLACED
    private final String status;

    ReportItem(String word, Integer count, Integer percentage, String status) {
        this.word = word;
        this.count = count;
        this.percentage = percentage;
        this.status = status;
    }

    public static ReportItemBuilder builder() {
        return new ReportItemBuilder();
    }

    public String getWord() {
        return this.word;
    }

    public Integer getCount() {
        return this.count;
    }

    public Integer getPercentage() {
        return this.percentage;
    }

    public String getStatus() { return status; }

    @NotNull
    public String toString() {
        return "ReportItem(word=" + this.getWord() + ", count=" + this.getCount() + ", percentage=" + this.getPercentage() + ", status=" + status + ")";
    }

    public static class ReportItemBuilder {
        private String word;
        private Integer count;
        private Integer percentage;
        private String status;

        ReportItemBuilder() {
        }

        public ReportItemBuilder word(String word) {
            this.word = word;
            return this;
        }

        public ReportItemBuilder count(Integer count) {
            this.count = count;
            return this;
        }

        public ReportItemBuilder percentage(Integer percentage) {
            this.percentage = percentage;
            return this;
        }

        public ReportItemBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ReportItem build() {
            return new ReportItem(word, count, percentage, status);
        }

    }
}
