package com.gunnarro.android.ughme.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gunnarro.android.ughme.model.config.Settings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Settings.class}, version = 1, exportSchema = false)
public abstract class SettingsDatabase extends RoomDatabase {

    public abstract SettingsDao settingsDao();

    // marking the instance as volatile to ensure atomic access to the variable
    private static volatile SettingsDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 2;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static SettingsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SettingsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                            SettingsDatabase.class, "settings_database")
                            .addCallback(settingsDatabaseCallback)
                            .build();
                    Log.d("SettingsDatabase", "created settings database");
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Override the onCreate method to populate the database.
     */
    private static final RoomDatabase.Callback settingsDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Populate the database with default settings in the background.
                // SettingsDao dao = INSTANCE.settingsDao();
                // dao.insert(new Settings());
            });
            Log.d("SettingsDatabase", "added default settings");
        }
    };
}
