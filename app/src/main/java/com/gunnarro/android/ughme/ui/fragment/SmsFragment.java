package com.gunnarro.android.ughme.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.sms.SmsReader;
import com.gunnarro.android.ughme.ui.fragment.domain.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmsFragment extends Fragment implements View.OnClickListener, DialogActionListener {
    private static final String LOG_TAG = SmsFragment.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_CODE_READ_SMS = 22;
    public static final String ALL = "all";
    private ProgressDialog progressDialog;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static SmsFragment newInstance(String param1, String param2) {
        SmsFragment fragment = new SmsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        view.findViewById(R.id.btn_sms_export_btn).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup_info_btn).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_delete_backup_file_btn).setOnClickListener(this);

        view.findViewById(R.id.btn_sms_backup_btn).setOnClickListener(v -> {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Start backup sms ..."); // Setting Message
            progressDialog.setTitle("SMS Backup Progress"); // Setting Title
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.show(); // Display Progress Dialog
            progressDialog.setCancelable(false);
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }).start();
            backupSmsInbox(ALL);
        });
        Log.d("SmsFragment", "onCreateView");
        return view;
    }



    private List<Sms> getSmsInbox(String mobileNumber) {
        SmsReader smsReader = new SmsReader(Objects.requireNonNull(getActivity()).getApplicationContext());
        List<Sms> inbox = smsReader.getSMSInbox(false, mobileNumber);
        Log.d(LOG_TAG, "getSmsInbox: sms for mobile number: " + mobileNumber + ", number of sms: " + inbox.size());
        return inbox;
    }

    private void backupSmsInbox(String mobileNumber) {
        try {
            Log.d(LOG_TAG, String.format("backup sms, mobilenumber: %s", mobileNumber));
            String filePath = getSmsBackupFilePath(mobileNumber);
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            List<Sms> inbox = getSmsInbox(mobileNumber);
            List<Sms> smsBackupList = getSmsBackup(mobileNumber);
            List<Sms> newSmsList = diffLists(inbox, new ArrayList<>(smsBackupList));
            if (!newSmsList.isEmpty()) {
                smsBackupList.addAll(newSmsList);
                smsBackupList.sort((Sms s1, Sms s2) -> s1.getTimeMs().compareTo(s2.getTimeMs()));
                Log.d(LOG_TAG, String.format("backupSmsInbox: Update backup, new sms: %s, current: %s", newSmsList.size(), smsBackupList.size()));
                FileWriter fw = new FileWriter(filePath, false);
                gson.toJson(smsBackupList, fw);
                fw.flush();
                fw.close();
                Log.d(LOG_TAG, String.format("backupSmsInbox: Saved sms (%s) backup, path: %s", inbox.size(), filePath));
                Snackbar.make(Objects.requireNonNull(getView()), String.format("Saved sms (%s) backup to %s", inbox.size(), filePath), Snackbar.LENGTH_LONG).show();
                progressDialog.setDismissMessage(null);
                progressDialog.setDismissMessage(null);
            } else {
                Log.d(LOG_TAG, String.format("backupSmsInbox: Backup up to date, %s", filePath));
                Snackbar.make(Objects.requireNonNull(getView()), String.format("Backup up to date, %s", filePath), Snackbar.LENGTH_LONG).show();
            }
            SmsBackupInfo info = new SmsBackupInfo();
            info.setSmsBackupFilePath(filePath);
            info.setFromDateTime(smsBackupList.get(0).getTimeMs());
            info.setToDateTime(smsBackupList.get(smsBackupList.size()-1).getTimeMs());
            info.setNumberOfSms(smsBackupList.size());
            FileWriter fw = new FileWriter(getSmsBackupFilePath(mobileNumber+"-metadata"), false);
            gson.toJson(info
                    , fw);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            Snackbar.make(Objects.requireNonNull(getView()), String.format("Backup sms error" +
                    ", %s", e.getMessage()), Snackbar.LENGTH_LONG).show();
        } finally {
            progressDialog.dismiss();
        }
    }

    private List<Sms> diffLists(List<Sms> smsInbox, List<Sms> smsBackup) {
        if (smsBackup == null) {
            Log.d(LOG_TAG, "diffLists: no backup, return inbox");
            return smsInbox;
        }
        Log.d(LOG_TAG, String.format("diffLists: diff sms inbox (%s) and sms backup (%s)", smsInbox.size(), smsBackup.size()));
        List<Sms> diffList = new ArrayList<>();
        // Get already backed sms from inbox,
        List<Sms> unChangedObjects = smsInbox.stream().filter(smsBackup::contains).distinct().collect(Collectors.toList());
        // Remove unchanged objects from both lists
        smsInbox.removeAll(unChangedObjects);
        smsBackup.removeAll(unChangedObjects);
        if (smsInbox.equals(smsBackup)) {
            Log.d(LOG_TAG, "diffLists: sms inbox and sms backup are equal!");
        } else {
            diffList = smsInbox;
        }
        Log.d(LOG_TAG, String.format("diffLists: Number of sms diff: %s", diffList.size()));
        return diffList;
    }

    private String getSmsBackupFilePath(String mobileNumber) throws FileNotFoundException {
        File appDir = getSmsBackupDir();
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new FileNotFoundException("No sms backup file for mobile number found!");
        }
        String filePath = String.format("%s/sms-backup-%s.json", appDir.getPath(), mobileNumber);
        Log.d(LOG_TAG, String.format("getSmsBackupFilePath: %s", filePath));
        return filePath;
    }

    private List<Sms> getSmsBackup(String mobileNumber) {
        Log.d(LOG_TAG, String.format("getSmsBackup: for number: %s", mobileNumber));
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();
        try {
            File f = new File(getSmsBackupFilePath(mobileNumber));
            return gson.fromJson(new FileReader(f.getPath()), smsListType);
        } catch (FileNotFoundException e) {
            Snackbar.make(Objects.requireNonNull(getView()), "sms backup file not found! error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return new ArrayList<>();
        }
    }

    private void viewSmsBackupFileInfo() {
        try {
            File appDir = getSmsBackupDir();
            List<String> files = Arrays.asList(Objects.requireNonNull(appDir.list(getJsonFileNameFilter())));
            Log.d(LOG_TAG, String.format("viewSmsBackupFiles: number of files: %s", files.size()));
            Gson gson = new GsonBuilder().setLenient().create();
            Type smsListType = new TypeToken<SmsBackupInfo>() {
            }.getType();
            SmsBackupInfo info;
            File f = new File(getSmsBackupFilePath("all-metadata"));
            if (f.exists()) {
                info = gson.fromJson(new FileReader(f.getPath()), smsListType);
            } else {
                info = new SmsBackupInfo();
                info.setSmsBackupFilePath("No backup files found!");
            }
            // Create the AlertDialog object and return it
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

            // set the custom layout
            final View smsBackupInfoLayout = getLayoutInflater().inflate(R.layout.dlg_sms_backup_info, null);
            builder.setView(smsBackupInfoLayout);
            TextView filePathView = smsBackupInfoLayout.findViewById(R.id.file_path_value);
            filePathView.setText(info.getSmsBackupFilePath());
            TextView smsFromDateView = smsBackupInfoLayout.findViewById(R.id.sms_from_date_value);
            smsFromDateView.setText( Utility.formatTime(info.getFromDateTime()));
            TextView smsToDateView = smsBackupInfoLayout.findViewById(R.id.sms_to_date_value);
            smsToDateView.setText(Utility.formatTime(info.getToDateTime()));
            TextView smsNumberView = smsBackupInfoLayout.findViewById(R.id.number_of_sms_value);
            smsNumberView.setText(String.format("%s", info.getNumberOfSms()));
            TextView mobileView = smsBackupInfoLayout.findViewById(R.id.number_of_mobile_nr_value);
            mobileView.setText(String.format("%s", info.getNumberOfMobileNumbers()));

            builder.setTitle("SMS Backup Information");
            builder.setPositiveButton("Ok", (dialog, id) -> {
                // do nothing
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void deleteSmsBackupFiles() {
        try {
            File bckDir = getSmsBackupDir();
            if (bckDir.exists()) {
                for (File f : Objects.requireNonNull(bckDir.listFiles(getJsonFileNameFilter()))) {
                    if (f.delete()) {
                        Log.d(LOG_TAG, String.format("deleteSmsBackupFiles: Deleted file: %s", f.getName()));
                    } else {
                        Log.d(LOG_TAG, String.format("deleteSmsBackupFiles: Failed delete file: %s", f.getName()));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("deleteSmsBackupFiles: Error deleting backup file! %s", e.getLocalizedMessage()));
        }
    }

    private FilenameFilter getJsonFileNameFilter() {
        return (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.endsWith(".json");
        };
    }

    private File getSmsBackupDir() {
        return Objects.requireNonNull(getActivity()).getFilesDir();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sms_export_btn:
                List<Sms> inbox = getSmsInbox(ALL);
                RxBus.getInstance().publish(inbox);
                Snackbar.make(Objects.requireNonNull(getView()), String.format("Published sms backup history (%s)", inbox.size()), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_sms_backup_btn:
                backupSmsInbox(ALL);
                break;
            case R.id.btn_sms_backup_info_btn:
                viewSmsBackupFileInfo();
                break;
            case R.id.btn_sms_delete_backup_file_btn:
                DialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Confirm Delete SMS Backup Files", "Are You Sure?");
                confirmDialog.show(getChildFragmentManager(), "dialog");
                break;
        }
    }

    private boolean hasSmsPermission() {
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("sms", "request sms permission");
            super.requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_PERMISSIONS_CODE_READ_SMS);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE_READ_SMS) {
            if (permissions[0].equals(Manifest.permission.READ_SMS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("sms", "sms permission granted");
            }
        }
    }

    @Override
    public void onDialogAction(int actionCode) {
        Log.d(LOG_TAG, String.format("onDialogAction: action: %s", actionCode));
        if (actionCode == DialogActionListener.OK_ACTION) {
            // the user confirmed the operation
            deleteSmsBackupFiles();
            Snackbar.make(Objects.requireNonNull(getView()), "Deleted sms backup files.", Snackbar.LENGTH_LONG).show();
        }  else {
            // dismiss, do nothing, the user canceled the operation
            Log.d("sms", "delete sms backup file action cancelled by user");
        }
    }
}
