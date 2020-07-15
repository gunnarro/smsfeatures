package com.gunnarro.android.ughme.chart.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class DayAxisValueFormatter extends ValueFormatter {

    private final String[] WEEK_DAYS = new String[]{
            "Mo", "Th", "We", "Tu", "Fr", "Sa", "So"
    };

    public DayAxisValueFormatter() {
        // take parameters to change behavior of formatter
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        float percent = value / axis.mAxisRange;
        return WEEK_DAYS[(int) (WEEK_DAYS.length * percent)];
    }
}
