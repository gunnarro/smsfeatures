package com.gunnarro.android.ughme.ui;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
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
import com.gunnarro.android.ughme.ui.fragment.SettingsFragment;
import com.gunnarro.android.ughme.ui.fragment.SmsSearchFragment;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;

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
    SettingsFragment settingsFragment;
    @Inject
    PreferencesFragment preferencesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate, context: " + getApplicationContext());
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnClickListener(v -> viewFragment(smsSearchFragment));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                setTitle(string.title_wordcloud);
                viewFragment(wordCloudFragment);
            } else if (id == R.id.nav_settings) {
                setTitle(string.title_settings);
                viewFragment(preferencesFragment);
            }
            // close drawer after clicking the menu item
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleIntent(Intent intent) {
        Log.d("MainActivity", "handleIntent : " + intent.getAction());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            viewFragment(smsSearchFragment);
        }
    }

    private void viewFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id.content_frame, fragment)
                .commit();
    }

    private void checkPermissions() {
        // check and ask user for permission if not granted
        String[] permissions = new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // FIXME can only ask for one permission at time error: Can reqeust only one set of permissions at a time
        for (String permission : permissions) {
            if (super.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity.checkPermissions", String.format("Not Granted, send request: %s", permission));
                super.requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
            } else {
                // show dialog explaining why this permission is needed
                if (super.shouldShowRequestPermissionRationale(permission)) {
                    Log.i("MainActivity.checkPermissions", "explain why we need this permission! permission: " + permission);
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
        Log.d("MainActivity.onRequestPermissions", String.format("requestCode=%s, permissins=%s, grantResult=%s", requestCode, new ArrayList<>(Arrays.asList(permissions)), new ArrayList<>(Collections.singletonList(grantResults))));
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity.onRequestPermissions", String.format("permission granted for permission: %s", Arrays.asList(permissions)));
            } else {
                Log.i("MainActivity.onRequestPermissions", String.format("permission denied for permission: %s", Arrays.asList(permissions)));
            }
        }
    }
}