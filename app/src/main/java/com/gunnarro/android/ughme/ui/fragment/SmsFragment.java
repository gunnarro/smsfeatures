package com.gunnarro.android.ughme.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.sms.SmsReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmsFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SmsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SmsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SmsFragment newInstance(String param1, String param2) {
        SmsFragment fragment = new SmsFragment();
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
        Log.d("SmsFragment", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        view.findViewById(R.id.btn_sms_clipboard).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_export).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup_view).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup_files_view).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_delete_backup_file).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_search).setOnClickListener(this);
        Log.d("SmsFragment", "onCreateView");
        return view;
    }

    private List<Sms> getSmsInbox(String mobileNumber) {
        SmsReader smsReader = new SmsReader(getActivity().getApplicationContext());
        List<Sms> inbox = smsReader.getSMSInbox(false, mobileNumber);
        Log.d("getSmsInbox", "sms for mobile number: " + mobileNumber + ", number of sms: " + inbox.size());
        return inbox;
    }

    private void viewSmsInbox(String mobileNumber) {
        List<Sms> inbox = getSmsInbox(mobileNumber);
        Snackbar.make(getView(), "Sms inbox, number of messages: " + inbox.size(), Snackbar.LENGTH_LONG).show();
        TextView view = getActivity().findViewById(R.id.txt_sms_view);
        view.setText(String.format("%s", inbox));
    }

    private void copyToClipboard() {
        TextView view = getActivity().findViewById(R.id.txt_sms_view);
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("smsinbox", view.getText());
        if (clipboard != null) clipboard.setPrimaryClip(clip);
        Snackbar.make(getView(), "Copied sms to clipboard", Snackbar.LENGTH_LONG).show();
    }

    private void backupSmsInbox(String mobileNumber) {
        try {
            Log.d("SmsFragment", String.format("backup sms, %s", mobileNumber));
            String filePath = getSmsBackupFilePath(mobileNumber);
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            List<Sms> inbox = getSmsInbox(mobileNumber);
            List<Sms> smsBackupList = getSmsBackup(mobileNumber);
            List<Sms> newSmsList = diffLists(inbox, new ArrayList<>(smsBackupList));
            if (!newSmsList.isEmpty()) {
                smsBackupList.addAll(newSmsList);
                smsBackupList.sort((Sms s1, Sms s2) -> s1.getTimeMs().compareTo(s2.getTimeMs()));
                Log.d("SmsFragment", String.format("backupSmsInbox: Update backup, new sms: %s, current: %s", newSmsList.size(), smsBackupList.size()));
                FileWriter fw = new FileWriter(filePath, false);
                gson.toJson(smsBackupList, fw);
                fw.flush();
                fw.close();
                Log.d("SmsFragment", String.format("backupSmsInbox: Saved sms (%s) backup, path: %s", inbox.size(), filePath));
                Snackbar.make(getView(), String.format("Saved sms (%s) backup to %s", inbox.size(), filePath), Snackbar.LENGTH_LONG).show();
            } else {
                Log.d("SmsFragment", String.format("backupSmsInbox: Backup up to date, %s", filePath));
                Snackbar.make(getView(), String.format("Backup up to date, %s", filePath), Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(getView(), "sms backup failed! Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private List<Sms> diffLists(List<Sms> smsInbox, List<Sms> smsBackup) {
        if (smsBackup == null) {
            Log.d("SmsFragment", "diffLists: no backup, return inbox");
            return smsInbox;
        }
        Log.d("SmsFragment", String.format("diffLists: diff sms inbox (%s) and sms backup (%s)", smsInbox.size(), smsBackup.size()));
        List<Sms> diffList = new ArrayList<>();
        // Get already backed sms from inbox,
        List<Sms> unChangedObjects = smsInbox.stream().filter(smsBackup::contains).distinct().collect(Collectors.toList());
        // Remove unchanged objects from both lists
        smsInbox.removeAll(unChangedObjects);
        smsBackup.removeAll(unChangedObjects);
        if (smsInbox.equals(smsBackup)) {
            Log.d("SmsFragment", "diffLists: sms inbox and sms backup are equal!");
        } else {
            diffList = smsInbox;
        }
        Log.d("SmsFragment", String.format("diffLists: Number of sms diff: %s", diffList.size()));
        return diffList;
    }

    private List<Sms> getSmsBackup(String mobileNumber) {
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();

        try {
            File f = new File(getSmsBackupFilePath(mobileNumber));
            return gson.fromJson(new FileReader(f.getPath()), smsListType);
        } catch (FileNotFoundException e) {
            Snackbar.make(getView(), "sms backup file not found! error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return new ArrayList<>();
        }
    }

    private String getSmsBackupFilePath(String mobileNumber) throws FileNotFoundException {
        File appDir = getActivity().getFilesDir();
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new FileNotFoundException("No sms backup file for mobile number found!");
        }
        String filePath = String.format("%s/sms-backup-%s.json", appDir.getPath(), mobileNumber);
        Log.d("SmsFragment", String.format("getSmsBackupFilePath: %s", filePath));
        return filePath;
    }

    private String getInputMobileNumber() {
        EditText mobileNumber = getActivity().findViewById(R.id.inp_sms_mobile_number);
        if (mobileNumber.getText().toString().isEmpty()) {
            return "all";
        }
        return mobileNumber.getText().toString();
    }

    private String getFileContent(String filePath) throws FileNotFoundException {
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (Exception e) {
                Log.e("getFileContent", e.getMessage());
            }
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    private void viewSmsBackupFiles() {
        FilenameFilter jsonFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".json")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        File appDir = getActivity().getFilesDir();
        TextView tv = getActivity().findViewById(R.id.txt_sms_view);
        tv.setText(String.format("%s", appDir.list(jsonFilter).toString()));
    }

    private void deleteSmsBackupFiles() {
        FilenameFilter jsonFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".json")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        try {
            File bckDir = getActivity().getFilesDir();
            if (bckDir.exists()) {
                for (File f : bckDir.listFiles(jsonFilter)) {
                    f.delete();
                    Log.d("SmsFragment", String.format("deleteSmsBackupFiles: Deleted file: %s", f.getName()));
                }
            }
            TextView dd = getActivity().findViewById(R.id.txt_sms_view);
            dd.setText(String.format("deleteSmsBackupFiles: Deleted all json backup files: %s", bckDir.getPath()));
        } catch (Exception e) {
            Log.e("SmsFragment", String.format("deleteSmsBackupFiles: Error deleting backup file", e.getLocalizedMessage()));
        }
    }

    private void searchSms() {
        TextView seaarchAfter = getActivity().findViewById(R.id.inp_sms_mobile_number);
        List<Sms> smsList = getSmsInbox(null);
        Log.d("SmsFragment", String.format("searchSms: number of sms: %s, search after: %s", smsList.size(), seaarchAfter.getText().toString()));
        List<Sms> results = smsList.stream().filter(sms -> sms.getBody().contains(seaarchAfter.getText().toString())).collect(Collectors.toList());
        TextView view = getActivity().findViewById(R.id.txt_sms_view);
        view.setText(String.format("Search after: %s\n %s", seaarchAfter.getText().toString(), results));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sms_clipboard:
                copyToClipboard();
                break;
            case R.id.btn_sms_export:
                viewSmsInbox(getInputMobileNumber());
                break;
            case R.id.btn_sms_backup:
                backupSmsInbox(getInputMobileNumber());
                break;
            case R.id.btn_sms_backup_view:
                List<Sms> smsList = getSmsBackup(getInputMobileNumber());
                RxBus.getInstance().publish(smsList);
                Snackbar.make(getView(), String.format("Published sms backup history (%s)", smsList.size()), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_sms_backup_files_view:
                viewSmsBackupFiles();
                break;
            case R.id.btn_sms_delete_backup_file:
                deleteSmsBackupFiles();
                break;
            case R.id.btn_sms_search:
                searchSms();
                break;
        }
    }
}
