package com.gunnarro.android.ughme.chart;

public class StackedBarEntry {

    private float[] values;
    private String[] labels;

    public StackedBarEntry(String[] labels, float[] values) {
        this.labels = labels;
        this.values = values;
    }

    public String[] getLabels() {
        return labels;
    }

    public float[] getValues() {
        return values;
    }
}
