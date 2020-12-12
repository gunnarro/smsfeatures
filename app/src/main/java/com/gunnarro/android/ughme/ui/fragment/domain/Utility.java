package com.gunnarro.android.ughme.ui.fragment.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.UK);

    public static String formatTime(long timeMs) {
        return dateFormat.format(new Date(timeMs));
    }
}
