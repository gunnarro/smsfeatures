package com.gunnarro.android.ughme.repository;

import android.content.Context;
import android.util.Log;

import com.gunnarro.android.ughme.model.config.Settings;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SettingsRepository {

    private final SettingsDao settingsDao;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    @Inject
    public SettingsRepository(@ApplicationContext Context context) {
        Log.d("SettingsRepository", "init database..");
        SettingsDatabase db = SettingsDatabase.getDatabase(context);
        settingsDao = db.settingsDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public Settings getSettings() {
        return settingsDao.getAll().get(0);
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Settings settings) {
        SettingsDatabase.databaseWriteExecutor.execute(() -> settingsDao.insert(settings));
    }

    public void update(Settings settings) {
        SettingsDatabase.databaseWriteExecutor.execute(() -> settingsDao.update(settings));
    }

    public void delete(Settings settings) {
        SettingsDatabase.databaseWriteExecutor.execute(() -> settingsDao.delete(settings));
    }
}
