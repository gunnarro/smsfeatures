package com.gunnarro.android.ughme.chart.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MonthXAxisFormatter extends ValueFormatter {
    private final String[] MONTHS = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };

    public MonthXAxisFormatter() {
        // take parameters to change behavior of formatter
    }
/**
    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        float percent = value / axis.mAxisRange;
        return MONTHS[(int) (MONTHS.length * percent)];
    }
    */
}
