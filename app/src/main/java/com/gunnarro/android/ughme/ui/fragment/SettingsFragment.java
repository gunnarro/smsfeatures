package com.gunnarro.android.ughme.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.repository.SettingsRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Inject
    SettingsRepository settingsRepository;

    @Inject
    public SettingsFragment() {
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
        Log.d(LOG_TAG, "onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSettingsTask();
    }

    private void updateSettingsView(Settings settings) {
        Log.d(LOG_TAG, String.format("updateSettingsView: %s", settings));
    }

    @Override
    public void onClick(View view) {
    }

    private void getSettingsTask() {
        class GetSettingsTask extends AsyncTask<Void, Void, Settings> {
            @Override
            protected Settings doInBackground(Void... voids) {
                return settingsRepository.getSettings();
            }

            @Override
            protected void onPostExecute(Settings settings) {
                super.onPostExecute(settings);
                updateSettingsView(settings);
            }
        }
        GetSettingsTask task = new GetSettingsTask();
        task.execute();
    }

    private void saveSettingsTask(Settings settings) {
        class SaveSettingsTask extends AsyncTask<Void, Void, Settings> {
            @Override
            protected Settings doInBackground(Void... voids) {
                return settingsRepository.getSettings();
            }

            @Override
            protected void onPostExecute(Settings settings) {
                super.onPostExecute(settings);
            }
        }
        SaveSettingsTask task = new SaveSettingsTask();
        task.execute();
    }
}
