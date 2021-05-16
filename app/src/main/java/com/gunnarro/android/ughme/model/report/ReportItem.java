package com.gunnarro.android.ughme.model.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.jetbrains.annotations.NotNull;

/**
 * Delombok following annotations
 * <p>
 * ToString
 * Getter
 * Builder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportItem {

    private String word;
    private int count;
    private int percentage;
    // can be PLACED and NOT_PLACED
    private String status;
    private Integer category;

    /**
     * Used by jackson
     */
    ReportItem() {
    }

    ReportItem(String word, int count, int percentage, String status, Integer category) {
        this.word = word;
        this.count = count;
        this.percentage = percentage;
        this.status = status;
        this.category = category;
    }

    public static ReportItemBuilder builder() {
        return new ReportItemBuilder();
    }

    public String getWord() {
        return this.word;
    }

    public int getCount() {
        return this.count;
    }

    public int getPercentage() {
        return this.percentage;
    }

    public void setStatus(boolean isPlaced) {
        status = isPlaced ? "PLACED" : "NOT_PLACED";
    }

    public Integer getCategory() { return this.category; }

    @NotNull
    public String toString() {
        return "ReportItem(word=" + this.getWord() + ", count=" + this.getCount() + ", percentage=" + this.getPercentage() + ", status=" + status + ")";
    }

    public static class ReportItemBuilder {
        private String word;
        private int count;
        private int percentage;
        private String status;
        private Integer category;

        ReportItemBuilder() {
        }

        public ReportItemBuilder word(String word) {
            this.word = word;
            return this;
        }

        public ReportItemBuilder count(int count) {
            this.count = count;
            return this;
        }

        public ReportItemBuilder percentage(int percentage) {
            this.percentage = percentage;
            return this;
        }

        public ReportItemBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ReportItemBuilder category(Integer category) {
            this.category = category;
            return this;
        }

        public ReportItem build() {
            return new ReportItem(word, count, percentage, status, category);
        }

    }
}
