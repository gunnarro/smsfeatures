package com.gunnarro.android.ughme.ui.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.gunnarro.android.ughme.R;

import javax.inject.Inject;

public class PreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Inject
    public PreferencesFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.setPreferencesFromResource(R.xml.preferences, rootKey);
        getPreferenceManager().findPreference(getResources().getString(R.string.pref_number_of_words)).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(getResources().getString(R.string.pref_word_max_font_size)).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d("PreferencesFragment", String.format("onPreferenceChange, preference key=%s, new value=%s", preference.getKey(), newValue));
        // update value
        return true;
    }
}
