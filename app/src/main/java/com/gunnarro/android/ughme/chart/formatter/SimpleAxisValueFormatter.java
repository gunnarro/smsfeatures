package com.gunnarro.android.ughme.chart.formatter;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;

public class SimpleAxisValueFormatter extends ValueFormatter {

    private static final String TAG = SimpleAxisValueFormatter.class.getName();

    private String[] X_AXIS_VALUES = new String[]{
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
    };

    public SimpleAxisValueFormatter(String[] xAxisValues) {
        // take parameters to change behavior of formatter
        if (xAxisValues != null) {
            X_AXIS_VALUES = xAxisValues;
            Log.d(TAG, String.format("%s", Arrays.asList(X_AXIS_VALUES)));
        }
    }

    /**
    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        float percent = value / axis.mAxisRange;
        Log.d(TAG, "value: " + value + ", range: " + axis.mAxisRange);
        return X_AXIS_VALUES[(int) ((X_AXIS_VALUES.length-1) * percent)];
    }
    */
}
