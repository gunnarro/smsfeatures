package com.gunnarro.android.ughme.chart.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class YearXAxisFormatter extends ValueFormatter {
    private final String[] YEARS = new String[]{
            "2020", "2021", "2022", "2023", "2024", "2025"
    };

    public YearXAxisFormatter() {
        // take parameters to change behavior of formatter
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        float percent = value / axis.mAxisRange;
        return YEARS[(int) (YEARS.length * percent)];
    }
}
