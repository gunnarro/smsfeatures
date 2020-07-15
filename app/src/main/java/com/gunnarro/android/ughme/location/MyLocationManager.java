package com.gunnarro.android.ughme.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyLocationManager {
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private final static Map<String, List<Position>> historyMap = new HashMap<>();

    /**
     * Using the above code and Criteria, the best provider will be GPS_PROVIDER, where both GPS and NETWORK are available.
     * However if the GPS is turned off, the NETWORK_PROVIDER will be chosen and returned as the best provider.
     *
     * @param context
     * @return lungitude and latitude
     */
    public static Position getLocationLastKnown(Context context, String mobileNumber) {
        Log.d("MyLocationManager", String.format("getLocationLastKnown: get location for: %s", mobileNumber));
        Location location = null;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            String provider = locationManager.getBestProvider(criteria, true);
            Log.d("MyLocationManager", String.format("getLocationLastKnown: location service not available, try provider: %s", provider));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(provider);
            } else {
                Log.d("MyLocationManager", "getLocationLastKnown: Missing permission to location service");
            }
            if (location != null) {
                Position position = new Position(mobileNumber, location.getLatitude(), location.getLongitude(), location.getTime());
                position.setAltitude((int) location.getAltitude());
                String address = getAddress(location, context);
                if (address != null) {
                    position.setAddress(address);
                }
                updateTraceLocationHistory(position.getMobileNumber(), position);
                return position;
            } else {
                Log.d("MyLocationManager", String.format("getLocationLastKnown: Location not found! %s", mobileNumber));
            }
        }
        return null;
    }

    private static void updateTraceLocationHistory(String key, Position position) {
        if (!historyMap.containsKey(key)) {
            historyMap.put(key, new ArrayList<>(Arrays.asList(position)));
        } else {
            historyMap.get(key).add(position);
        }
    }

    /**
     * google maps lookup:
     * http://maps.googleapis.com/maps/api/geocode/json?latlng
     * =40.714224,-73.961452&sensor=true_or_false
     * <p>
     * Doc: http://code.google.com/apis/maps/documentation/geocoding/
     *
     * @return
     */
    private static String getAddress(Location location, Context context) {
        if (location == null) {
            Log.e("MyLocationManager", "getAddress: location is null");
            return null;
        }
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            StringBuilder address = new StringBuilder();
            //address.append("Used ").append(location.getProvider()).append(" provider:\n");
            //address.append("Last known address:\n");
            //SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy hh:ss:mm");
            //address.append(sd.format(location.getTime())).append("\n");
            if (addresses.size() > 0) {
                for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                    address.append(addresses.get(0).getAddressLine(i));
                    if (i < addresses.get(0).getMaxAddressLineIndex() - 1) {
                        address.append(",");
                    }
                }
            }
            return address.toString();
        } catch (Exception e) {
            Log.e("MyLocationManager", "getAddress: Error getting address:" + e.toString());
            return null;
        }
    }

    public List<Position> getPositions(String mobileNumber) {
        return historyMap.get(mobileNumber);
    }

    /**
     * @author gunnarro
     */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.i("MyLocationManager",
                    String.format("onLocationChanged: %s, new Location, Longitude: %1$s, Latitude: %2$s", location.getProvider(), location.getLongitude(), location.getLatitude()));
            // A new location updates has arrived
            // Remove the listener for location updates
            // locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle b) {
            Log.i("MyLocationManager", s + "onStatusChanged: provider status changed");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.i("MyLocationManager", "onProviderDisabled: Provider disabled by the user." + s + " turned off.");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.i("MyLocationManager", "onProviderEnabled: Provider enabled by the user." + s + " GPS turned on.");
        }

    }
}
