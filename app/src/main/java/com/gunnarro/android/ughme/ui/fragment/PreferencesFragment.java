package com.gunnarro.android.ughme.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.MainActivity;

import java.util.Objects;

import javax.inject.Inject;

public class PreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Inject
    public PreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference button = getPreferenceManager().findPreference("settings_back_button");
        if (button != null) {
            button.setOnPreferenceClickListener(arg0 -> {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setAction("view_word_cloud");
                startActivity(intent);
                return true;
            });
        }

        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_number_of_words))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_word_min_chars))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_word_max_font_size))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_word_min_font_size))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_radius_step))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_offset_step))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_word_rotate))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_color_schema))).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(Objects.requireNonNull(getResources().getString(R.string.pref_font_type))).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d("PreferencesFragment", String.format("onPreferenceChange, preference key=%s, new value=%s", preference.getKey(), newValue));
        // update value
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        Log.d("PreferencesFragment", "onCreateOptionsMenu. hasVisible=" + menu.hasVisibleItems());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // hide current options menu
        menu.getItem(0).setVisible(false);
    }
}
