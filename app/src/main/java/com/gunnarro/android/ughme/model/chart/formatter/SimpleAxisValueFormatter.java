package com.gunnarro.android.ughme.model.chart.formatter;

import android.util.Log;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;

public class SimpleAxisValueFormatter extends ValueFormatter {

    private static final String TAG = SimpleAxisValueFormatter.class.getName();

    public SimpleAxisValueFormatter(String[] xAxisValues) {
        // take parameters to change behavior of formatter
        if (xAxisValues != null) {
            Log.d(TAG, String.format("%s", Arrays.asList(xAxisValues)));
        }
    }
}
