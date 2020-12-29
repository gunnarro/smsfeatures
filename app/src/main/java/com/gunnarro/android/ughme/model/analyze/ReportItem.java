package com.gunnarro.android.ughme.model.analyze;

import org.jetbrains.annotations.NotNull;

public class ReportItem {

    private final String word;
    private final Integer count;
    private final Integer percentage;

    public ReportItem(Builder builder) {
        this.word = builder.word;
        this.count = builder.count;
        this.percentage = builder.percentage;
    }

    public static ReportItem.Builder builder() {
        return new ReportItem.Builder();
    }

    public String getWord() {
        return word;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getPercentage() {
        return percentage;
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReportItem{");
        sb.append("word='").append(word).append('\'');
        sb.append(", count=").append(count);
        sb.append(", percentage=").append(percentage);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder class
     */
    public static class Builder {
        private String word;
        private Integer count;
        private Integer percentage;

        private Builder() {
        }

        public ReportItem.Builder word(String word) {
            this.word = word;
            return this;
        }


        public ReportItem.Builder count(Integer count) {
            this.count = count;
            return this;
        }


        public ReportItem.Builder percentage(Integer percentage) {
            this.percentage = percentage;
            return this;
        }

        public ReportItem.Builder of(ReportItem reportItem) {
            this.word = reportItem.word;
            this.count = reportItem.count;
            this.percentage = reportItem.percentage;
            return this;
        }

        public ReportItem build() {
            return new ReportItem(this);
        }
    }
}
