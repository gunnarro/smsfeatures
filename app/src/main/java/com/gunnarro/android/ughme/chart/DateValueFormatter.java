package com.gunnarro.android.ughme.chart;

import com.github.mikephil.charting.formatter.ValueFormatter;

import static java.lang.Float.valueOf;

public class DateValueFormatter extends ValueFormatter {

    private static final String[] WEEK_DAYS = new String[]{"Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su"};
    private static final String[] MONTHS = new String[]{"Jan", "Feb", "Mar", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final String[] YEARS = new String[]{"2020", "2021", "2022", "2023", "2024"};

    @Override
    public String getFormattedValue(float value) {
        // LocalDateTime d = LocalDateTime.ofInstant(Instant.ofEpochMilli(value),null);
        return WEEK_DAYS[valueOf(value).intValue()];
    }
}
