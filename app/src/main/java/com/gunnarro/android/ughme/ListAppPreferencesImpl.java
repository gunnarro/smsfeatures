package com.gunnarro.android.ughme;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListAppPreferencesImpl extends AppPreferences {
    private final String type;

    public ListAppPreferencesImpl(Context context, String type) {
        super(context);
        this.type = type;
    }

    public List<String> getList() {
        String list = getAppSharedPrefs().getString(type, DEFAULT_VALUE);
        if (list != null) {
            return new ArrayList<>(Arrays.asList(list.split(SEPARATOR)));
        }
        return new ArrayList<>();
    }

    public String getListAsString() {
        return getAppSharedPrefs().getString(type, DEFAULT_VALUE);
    }

    public boolean removeAllList() {
        getPrefsEditor().putString(type, DEFAULT_VALUE);
        return getPrefsEditor().commit();
    }

    public boolean updateList(String item) {
        StringBuilder blackListedNumbers = new StringBuilder(getListAsString());
        if (blackListedNumbers.length() == 0) {
            blackListedNumbers.append(item);
        } else if (!blackListedNumbers.toString().contains(item) && item.length() > 1) {
            blackListedNumbers.append(SEPARATOR).append(item);
        }
        getPrefsEditor().putString(type, blackListedNumbers.toString());
        return getPrefsEditor().commit();
    }

    public boolean removeList(String item) {
        List<String> smsBlackList = getList();
        if (!smsBlackList.isEmpty() && smsBlackList.contains(item)) {
            StringBuilder blackListedNumbers = new StringBuilder();
            int i = smsBlackList.size();
            for (String blackListedNumber : smsBlackList) {
                i--;
                if (!blackListedNumber.equals(item)) {
                    blackListedNumbers.append(blackListedNumber);
                    if (i > 0) {
                        blackListedNumbers.append(SEPARATOR);
                    }
                }
            }
            getPrefsEditor().putString(type, blackListedNumbers.toString());
            return getPrefsEditor().commit();
        }
        return false;
    }

    public boolean listContains(String item) {
        for (String blackListedPhoneNumber : getList()) {
            if (blackListedPhoneNumber.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
