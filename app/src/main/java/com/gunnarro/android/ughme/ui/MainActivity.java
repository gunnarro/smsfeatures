package com.gunnarro.android.ughme.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.service.SmsBackupService;
import com.gunnarro.android.ughme.ui.fragment.BackupFragment;
import com.gunnarro.android.ughme.ui.fragment.BarChartFragment;
import com.gunnarro.android.ughme.ui.fragment.SettingsFragment;
import com.gunnarro.android.ughme.ui.fragment.SmsSearchFragment;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;

import java.util.ArrayList;
import java.util.Arrays;

import static com.gunnarro.android.ughme.R.id;
import static com.gunnarro.android.ughme.R.layout;
import static com.gunnarro.android.ughme.R.string;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int PERMISSION_REQUEST = 1;

    private DrawerLayout drawer;
    private FragmentManager fm;
    private SmsBackupService smsBackupService;

    private static Context context;

    public static Context getAppContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate, context: " + getApplicationContext());
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        smsBackupService = new SmsBackupService(Environment.getExternalStorageDirectory());
        setContentView(layout.activity_main);
        ActionBar toolbar = getSupportActionBar();
        drawer = (DrawerLayout) findViewById(id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, string.app_name, string.app_name);
        //drawer.setDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        // disply home button for actionbar
        toolbar.setDisplayHomeAsUpEnabled(true);
        // navigation view select home menu by default
        navigationView.setCheckedItem(id.nav_sms_backup);
        fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            viewFragment(BackupFragment.newInstance(smsBackupService));
        }
        // Finally, check and grant or deni permissions
        checkPermissions();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// Open Close Drawer Layout
            if (drawer.isOpen()) {
                drawer.closeDrawers();
            } else {
                drawer.openDrawer(Gravity.LEFT);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        try {
            int id = menuItem.getItemId();
            if (id == R.id.nav_sms_backup) {
                viewFragment(BackupFragment.newInstance(smsBackupService));
            } else if (id == R.id.nav_sms_search) {
                viewFragment(SmsSearchFragment.newInstance(smsBackupService));
            } else if (id == R.id.nav_sms_chart) {
                viewFragment(BarChartFragment.newInstance(smsBackupService));
            } else if (id == R.id.nav_sms_wordcloud) {
                viewFragment(WordCloudFragment.newInstance(smsBackupService));
            } else if (id == R.id.nav_settings) {
                viewFragment(SettingsFragment.newInstance());
            }
            // close drawer after clicking the menu item
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void viewFragment(@NonNull Fragment fragment) {
        FragmentTransaction t = fm.beginTransaction();
        t.replace(id.content_frame, fragment);
        t.commit();
    }

    private void checkPermissions() {
        // check and ask user for permission if not granted
        String[] permissions = new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (super.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w("MainActivity.checkPermissions", String.format("Not Granted, send request: %s", permission));
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
        Log.d("MainActivity.onRequestPermission", String.format("requestCode=%s, permissins=%s", requestCode, new ArrayList<>(Arrays.asList(permissions))));
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