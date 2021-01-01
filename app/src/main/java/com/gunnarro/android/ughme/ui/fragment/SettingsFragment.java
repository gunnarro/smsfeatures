package com.gunnarro.android.ughme.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.cloud.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Properties;

import javax.inject.Inject;


public class SettingsFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    private Settings settings;

    @Inject
    public SettingsFragment() {
        /*
        InputStream is = context.getAssets().open("configuration.properties");
        Properties props = new Properties();
        props.load(is);
        String value = props.getProperty("key", "");
        is.close();
         */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        /*
        view.findViewById(R.id.setting_max_chars_in_word_sp).setOnClickListener(this);
        view.findViewById(R.id.setting_radius_step_sp).setOnClickListener(this);
        view.findViewById(R.id.setting_offset_step_sp).setOnClickListener(this);
        */
        Log.d(LOG_TAG, "onCreateView");
        return view;
    }

    private void loadSettings() throws IOException {
        Gson gson = new GsonBuilder().setLenient().create();
        Type settingsType = new TypeToken<Settings>() {
        }.getType();
        File f = getSettingsFile();
        if (!f.exists()) {
            // settings do not exist, create a new setting file with default settings
            f.createNewFile();
            FileWriter fw = new FileWriter(f.getPath(), false);
            gson.toJson(new Settings(), fw);
            fw.flush();
            fw.close();
            Log.d("MainActivity", String.format("created setting file: %s", f.getPath()));
        }
        // read the setting file
        Settings settings = gson.fromJson(new FileReader(f.getPath()), settingsType);
    }

    private void saveSettings() throws IOException {
        Gson gson = new GsonBuilder().setLenient().create();
        FileWriter fw = new FileWriter(getSettingsFile(), false);
        gson.toJson(settings, fw);
        fw.flush();
        fw.close();
    }

    private File getApplicationDir() {
        return Objects.requireNonNull(getContext()).getFilesDir();
    }

    private File getSettingsFile() {
        return new File(getApplicationDir().getPath().concat("/settings.json"));
    }

    @Override
    public void onClick(View view) {

    }
}
