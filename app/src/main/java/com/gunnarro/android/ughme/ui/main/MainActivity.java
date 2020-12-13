package com.gunnarro.android.ughme.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.ListFragmentInteractionListener;
import com.gunnarro.android.ughme.ui.fragment.domain.ListItem;
import com.gunnarro.android.ughme.ui.view.Settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ListFragmentInteractionListener {

    public static final int PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(tabsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // check and ask user for permission if not granted
        //String[] permissions = new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS};
        String[] permissions = new String[]{Manifest.permission.READ_SMS};
        for (String permission : permissions) {
            if (super.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w("MainActivity.onCreate", String.format("Not Granted, send request: %s", permission));
                super.requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
            } else {
                // show dialog explaining why this permission is needed
                if (super.shouldShowRequestPermissionRationale(permission)) {
                    Log.i("MainActivity.onCreate", "explain why we need this permission! permission: " + permission);
                }
            }
        }
    }

    /**
     * This function is called when user accept or decline the permission.
     * Request Code is used to check which permission called this function.
     * This request code is provided when user is prompt for permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("MainActivity.onRequestPermission", String.format("%s, %s", requestCode, new ArrayList<>(Arrays.asList(permissions))));
        if (requestCode == PERMISSION_REQUEST) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity.onRequestPermissions", String.format("permission granted for %s", permissions[0]));
            } else {
                Log.i("MainActivity.onRequestPermissions", String.format("permission denied for %s", permissions[0]));
            }
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#*} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void checkAndRequestPermission(final Activity activity, final String permission, String accessRequired, String accessDenied, String unavailable, final int permissionId) {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted and must be requested.
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // Display a SnackBar with cda button to request the missing permission.
                Snackbar.make(findViewById(R.id.content), accessRequired, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, view -> ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionId)).show();
            } else {
                Snackbar.make(findViewById(R.id.content), unavailable, Snackbar.LENGTH_LONG).show();
                // Request the permission. The result will be received in onRequestPermissionResult().
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionId);
            }
        }
    }

    private File getSmsBackupDir() {
        return Objects.requireNonNull(getApplicationContext()).getFilesDir();
    }

    private File getApplicationDir() {
        return Objects.requireNonNull(getApplicationContext()).getFilesDir();
    }

    private File getSettingsFile() {
        return new File(getApplicationDir().getPath().concat("/settings.json"));
    }

    @Override
    public void onListFragmentInteraction(ListItem item) {
        Log.d("MainActivity", String.format("onListFragmentInteraction: %s", item));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_settings:
                openSettingsDialog();
                break;
            // action with ID action_settings was selected
            case R.id.action_sms_backup_info:
                Toast.makeText(this, "backup selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.action_wordcloud_info:
                Toast.makeText(this, "wordcloud selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }
        return true;
    }

    private void openSettingsDialog() {
        try {
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
            // Create the AlertDialog object and return it
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getApplicationContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

            // set the custom layout
            final View settingsLayout = getLayoutInflater().inflate(R.layout.fragment_settings, null);
            builder.setView(settingsLayout);
           /*
            Spinner sp = settingsLayout.findViewById(R.id.setting_max_chars_in_word_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_min_chars_in_word_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_number_Of_bars_in_chart_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_number_of_mobile_numbers_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_number_Of_words_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_max_chars_in_word_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_offset_step_sp);
            Spinner sp = settingsLayout.findViewById(R.id.setting_radius_step_sp);
*/
            builder.setTitle("Sms Features Settings");
            builder.setPositiveButton("Ok", (dialog, id) -> {
                try {
                    FileWriter fw = new FileWriter(getSettingsFile(), false);
                    gson.toJson(settings, fw);
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, id) -> {
                // do nothing
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
        }
    }
}