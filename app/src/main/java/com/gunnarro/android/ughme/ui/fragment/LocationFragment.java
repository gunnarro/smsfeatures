package com.gunnarro.android.ughme.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.location.MyLocationManager;
import com.gunnarro.android.ughme.location.Position;
import com.gunnarro.android.ughme.service.UghmeIntentService;
import com.gunnarro.android.ughme.sms.SmsHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String SMS_TRACE_ACTION_CODE = "trace";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        view.findViewById(R.id.btn_get_location).setOnClickListener(this);
        view.findViewById(R.id.btn_view_location_history).setOnClickListener(this);
        return view;
    }

    private void displayLocation() {
        TextView mobileNumberView = getActivity().findViewById(R.id.inp_mobile_number);
        String mobileNumber = mobileNumberView.getText().toString();
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            displayMyLocation();
        } else if (!PhoneNumberUtils.isGlobalPhoneNumber(mobileNumber)) {
            Snackbar.make(getView(), "Invalid mobile number: " + mobileNumber, Snackbar.LENGTH_LONG).show();
        } else {
            displayLocationForMobileNumber(mobileNumber);
        }
    }

    private void displayMyLocation() {
        try {
            Log.d("location", "displayMyLocation: get my location");
            //checkAndRequestPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION, "Location access required", "Location access denied", "Location access unavailable", PERMISSION_REQUEST);
            String googleMapUrl = "na";
            Position myPosition = null;
            if (getContext() != null) {
                myPosition = MyLocationManager.getLocationLastKnown(getContext(), "45465500");
                if (myPosition != null) {
                    googleMapUrl = myPosition.createGoogleMapUrl();
                }
            }
            TextView locationView = getActivity().findViewById(R.id.view_location);
            locationView.setText(myPosition != null ? myPosition.toString() : "service not available");
            Snackbar.make(getView(), String.format("%s", googleMapUrl), Snackbar.LENGTH_LONG).show();
            if (myPosition != null) {
                saveLocationHistory(myPosition);
            }
        } catch (Exception e) {
            Log.e("location", e.getMessage());
            Toast.makeText(getContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void displayLocationForMobileNumber(String mobileNumber) {
        try {
            Log.d("location", "displayLocationForMobileNumber: get location for " + mobileNumber);
            Snackbar.make(getView(), "Trace: " + mobileNumber, Snackbar.LENGTH_LONG).show();
            // checkAndRequestPermission(getContext(), Manifest.permission.SEND_SMS, "send sms access required", "send sms access denied", "send sms access unavailable", PERMISSION_REQUEST_SMS);
            // checkAndRequestPermission(getContext(), Manifest.permission.READ_CONTACTS, "contacts access required", "contacts access denied", "contacts access unavailable", PERMISSION_REQUEST_CONTACTS);
            Intent intent = new Intent(getContext().getApplicationContext(), UghmeIntentService.class);
            intent.putExtra(SmsHandler.KEY_MOBILE_NUMBER, mobileNumber);
            intent.putExtra(SmsHandler.KEY_SMS_MSG, SMS_TRACE_ACTION_CODE);
            Snackbar.make(getView(), "Start trace number: " + mobileNumber, Snackbar.LENGTH_LONG).show();
            getActivity().startService(intent);
        } catch (Exception e) {
            Log.e("location", e.getMessage());
            Snackbar.make(getView(), "ERROR" + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void saveLocationHistory(Position position) {
        try {
            // get current location history list
            List<Position> locationHistoryList = getLocationHistory();
            locationHistoryList.add(position);
            // save updated location history list
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            FileWriter fw = new FileWriter(getLocationHistoryFilePath(), false);
            gson.toJson(locationHistoryList, fw);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            Log.e("", "Failed update location history! error: " + e.getMessage());
        }
    }

    private List<Position> getLocationHistory() {
        Gson gson = new GsonBuilder().setLenient().create();
        Type positionListType = new TypeToken<ArrayList<Position>>() {
        }.getType();
        try {
            File f = new File(getLocationHistoryFilePath());
            return gson.fromJson(new FileReader(f.getPath()), positionListType);
        } catch (FileNotFoundException e) {
            Snackbar.make(getView(), "location history file not found! error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return null;
        }
    }

    private String getLocationHistoryFilePath() throws FileNotFoundException {
        File appDir = getActivity().getFilesDir();
        return String.format("%s/location-history.json", appDir.getPath());
    }

    private void viewLocationHistory() {
        List<Position> list = getLocationHistory();
        TextView locationView = getActivity().findViewById(R.id.view_location);
        locationView.setText(list != null ? list.toString() : "location history not available");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get_location:
                displayLocation();
                break;
            case R.id.btn_view_location_history:
                viewLocationHistory();
                break;
        }
    }
}
