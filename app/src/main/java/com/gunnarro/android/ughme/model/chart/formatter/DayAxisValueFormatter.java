package com.gunnarro.android.ughme.model.chart.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class DayAxisValueFormatter extends ValueFormatter {

    private static final String[] WEEK_DAYS = new String[]{
            "Mo", "Th", "We", "Tu", "Fr", "Sa", "So"
    };

    public DayAxisValueFormatter() {
        // take parameters to change behavior of formatter
    }
}
