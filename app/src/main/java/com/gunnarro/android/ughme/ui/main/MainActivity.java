package com.gunnarro.android.ughme.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.ListFragmentInteractionListener;
import com.gunnarro.android.ughme.ui.fragment.domain.ListItem;

import java.util.ArrayList;
import java.util.Arrays;

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
        String[] permissions = new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS};
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w("MainActivity", String.format("Not Granted: %s", permission));
                requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
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
        Log.d("onRequestPermission", String.format("%s, %s", requestCode, new ArrayList<>(Arrays.asList(permissions))));
        if (requestCode == PERMISSION_REQUEST) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("onRequestPermissions", "permission granted for LOCATION");
            } else {
                Log.i("onRequestPermissions", "permission denied for LOCATION");
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

    @Override
    public void onListFragmentInteraction(ListItem item) {
        Log.d("MainActivty", String.format("onListFragmentInteraction: %s", item));
    }
}