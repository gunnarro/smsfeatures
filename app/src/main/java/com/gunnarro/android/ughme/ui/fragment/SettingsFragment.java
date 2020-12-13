package com.gunnarro.android.ughme.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.Utility;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.sms.SmsReader;
import com.gunnarro.android.ughme.ui.view.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    private Settings settings;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        Type settingsType = new TypeToken<Settings>() {}.getType();
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
