package com.gunnarro.android.ughme.model.analyze;

import org.jetbrains.annotations.NotNull;

/**
 * Delombok follwoing annotations
 * <p>
 * ToString
 * Getter
 * Builder
 */
public class ReportItem {

    private final String word;
    private final Integer count;
    private final Integer percentage;

    ReportItem(String word, Integer count, Integer percentage) {
        this.word = word;
        this.count = count;
        this.percentage = percentage;
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

    @NotNull
    public String toString() {
        return "ReportItem(word=" + this.getWord() + ", count=" + this.getCount() + ", percentage=" + this.getPercentage() + ")";
    }

    public static class ReportItemBuilder {
        private String word;
        private Integer count;
        private Integer percentage;

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

        public ReportItem build() {
            return new ReportItem(word, count, percentage);
        }

    }
}
