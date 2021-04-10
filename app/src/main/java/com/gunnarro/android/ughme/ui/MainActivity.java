package com.gunnarro.android.ughme.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.BackupFragment;
import com.gunnarro.android.ughme.ui.fragment.BarChartFragment;
import com.gunnarro.android.ughme.ui.fragment.PreferencesFragment;
import com.gunnarro.android.ughme.ui.fragment.ReportFragment;
import com.gunnarro.android.ughme.ui.fragment.SmsSearchFragment;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;
import com.gunnarro.android.ughme.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static com.gunnarro.android.ughme.R.id;
import static com.gunnarro.android.ughme.R.string;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int PERMISSION_REQUEST = 1;

    private DrawerLayout drawer;

    @Inject
    BackupFragment backupFragment;
    @Inject
    SmsSearchFragment smsSearchFragment;
    @Inject
    WordCloudFragment wordCloudFragment;
    @Inject
    BarChartFragment barChartFragment;
    @Inject
    PreferencesFragment preferencesFragment;
    @Inject
    ReportFragment reportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Utility.buildTag(getClass(), "onCreate"), "context: " + getApplicationContext());
        Log.d(Utility.buildTag(getClass(), "onCreate"), "app file dir: " + getApplicationContext().getFilesDir().getPath());

        if (!new File(getApplicationContext().getFilesDir().getPath()).exists()) {
            Log.d(Utility.buildTag(getClass(), "onCreate"), "app file dir missing! " + getApplicationContext().getFilesDir().getPath());
        }

        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Utility.buildTag(getClass(), "onCreate"), "Failed starting! " + e.getMessage());
        }
        drawer = findViewById(id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, string.title_backup, string.title_backup);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        // display home button for actionbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // navigation view select home menu by default
        navigationView.setCheckedItem(id.nav_sms_backup);
        if (savedInstanceState == null) {
            viewFragment(backupFragment);
        }
        // Finally, check and grant or deny permissions
        checkPermissions();
        handleIntent(getIntent());
    }

    /**
     * It's where you should place actions that have a global impact on the app.
     * Only create the initial menu state and not make changes during the activity lifecycle.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        // Associate searchable configuration with the SearchView
        // SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_sms).getActionView();
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setOnClickListener(v -> Log.d(TAG, "search clicked 1"));
        searchView.setOnSearchClickListener(v -> viewFragment(smsSearchFragment));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(Utility.buildTag(getClass(), "onOptionsItemSelected"), "selected: " + item);
        if (item.getItemId() == android.R.id.home) {// Open Close Drawer Layout
            if (drawer.isOpen()) {
                drawer.closeDrawers();
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        try {
            int id = menuItem.getItemId();
            if (id == R.id.nav_sms_backup) {
                setTitle(string.title_backup);
                viewFragment(backupFragment);
            } else if (id == R.id.nav_sms_search) {
                setTitle(string.title_search);
                viewFragment(smsSearchFragment);
            } else if (id == R.id.nav_sms_chart) {
                setTitle(string.title_chart);
                viewFragment(barChartFragment);
            } else if (id == R.id.nav_sms_wordcloud) {
                setTitle(string.title_word_cloud);
                viewFragment(wordCloudFragment);
            } else if (id == R.id.nav_settings) {
                setTitle(string.title_settings);
                viewFragment(preferencesFragment);
            } else if (id == R.id.nav_report) {
                setTitle(string.title_report);
                viewFragment(reportFragment);
            }
            // close drawer after clicking the menu item
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    private void handleIntent(Intent intent) {
        Log.d(Utility.buildTag(getClass(), "handleIntent"), "Action: " + intent.getAction());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            viewFragment(smsSearchFragment);
        }
    }

    private void viewFragment(@NonNull Fragment fragment) {
        Log.d(Utility.buildTag(getClass(), "viewFragment"), "fragment: " + fragment.getTag());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id.content_frame, fragment)
                .commit();
    }

    private void checkPermissions() {
        Log.i(Utility.buildTag(getClass(), "checkPermissions"), "Start check permissions...");
        // check and ask user for permission if not granted
        String[] permissions = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS};
        // FIXME For API 24 only ask for one permission at time error: Can request only one set of permissions at a time
        for (String permission : permissions) {
            if (super.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.i(Utility.buildTag(getClass(), "checkPermissions"), String.format("Not Granted, send request: %s", permission));
                super.requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
            } else {
                // show dialog explaining why this permission is needed
                if (super.shouldShowRequestPermissionRationale(permission)) {
                    Log.i(Utility.buildTag(getClass(), "checkPermissions"), "explain why we need this permission! permission: " + permission);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(Utility.buildTag(getClass(), "onRequestPermissions"), String.format("requestCode=%s, permission=%s, grantResult=%s", requestCode, new ArrayList<>(Arrays.asList(permissions)), new ArrayList<>(Collections.singletonList(grantResults))));
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(Utility.buildTag(getClass(), "onRequestPermissions"), String.format("permission granted for permission: %s", Arrays.asList(permissions)));
            } else {
                Log.i(Utility.buildTag(getClass(), "onRequestPermissions"), String.format("permission denied for permission: %s", Arrays.asList(permissions)));
            }
        }
    }
}