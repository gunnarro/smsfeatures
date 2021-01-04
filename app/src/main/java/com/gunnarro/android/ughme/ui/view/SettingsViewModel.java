package com.gunnarro.android.ughme.ui.view;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.repository.SettingsRepository;

public class SettingsViewModel extends AndroidViewModel {

    SettingsRepository repository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private final Settings settings;

    public SettingsViewModel(Application application) {
        super(application);
        repository = new SettingsRepository(application);
        settings = repository.getSettings();
    }

    Settings getSettings() {
        return settings;
    }

    void insert(Settings settings) {
        repository.insert(settings);
    }
}
