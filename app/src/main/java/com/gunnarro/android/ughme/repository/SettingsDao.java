package com.gunnarro.android.ughme.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gunnarro.android.ughme.model.config.Settings;

import java.util.List;

@Dao
public interface SettingsDao {

    @Query("SELECT * FROM settings_table")
    List<Settings> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Settings settings);

    @Update
    void update(Settings settings);

    @Delete
    void delete(Settings settings);

    @Query("DELETE FROM settings_table")
    void deleteAll();

}
