package com.gunnarro.android.ughme.model.chart.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class MonthXAxisFormatter extends ValueFormatter {
    private static final String[] MONTHS = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };

    public MonthXAxisFormatter() {
        // take parameters to change behavior of formatter
    }

}
