package com.gunnarro.android.ughme.model.chart;

public class StackedBarEntry {

    private final float[] values;
    private final String[] labels;

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
