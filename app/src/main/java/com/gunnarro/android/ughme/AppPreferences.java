package com.gunnarro.android.ughme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public abstract class AppPreferences {
    public static final String SMS_BLACK_LIST = "sms_blacklist";
    public static final String AUTHENTICATED_USERS = "autenticated_users";
    static final String DEFAULT_VALUE = "";
    static final String SEPARATOR = ",";
    private static final String APP_SHARED_PREFS = "user_preferences";
    private final SharedPreferences appSharedPrefs;
    private final SharedPreferences.Editor prefsEditor;

    AppPreferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public abstract List<String> getList();

    public abstract String getListAsString();

    public abstract boolean removeAllList();

    public abstract boolean updateList(String item);

    public abstract boolean removeList(String item);

    public abstract boolean listContains(String item);

    SharedPreferences getAppSharedPrefs() {
        return appSharedPrefs;
    }

    SharedPreferences.Editor getPrefsEditor() {
        return prefsEditor;
    }
}
