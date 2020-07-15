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

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.sms.SmsReader;
import com.gunnarro.android.ughme.ui.main.TabsPagerAdapter;

import java.io.BufferedReader;
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

    public static final String ALL = "All";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ViewPager viewPager;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1. public SmsFragment() {
     *         // Required empty public constructor
     *     }
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
        viewPager = Objects.requireNonNull(getActivity()).findViewById(R.id.view_pager);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        view.findViewById(R.id.btn_sms_clipboard).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_export).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_chart_view).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup_files_view).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_delete_backup_file).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_search).setOnClickListener(this);
        Log.d("SmsFragment", "onCreateView");
        return view;
    }

    private List<Sms> getSmsInbox(String mobileNumber) {
        SmsReader smsReader = new SmsReader(Objects.requireNonNull(getActivity()).getApplicationContext());
        List<Sms> inbox = smsReader.getSMSInbox(false, mobileNumber);
        Log.d(LOG_TAG, "getSmsInbox: sms for mobile number: " + mobileNumber + ", number of sms: " + inbox.size());
        return inbox;
    }

    private void copyToClipboard() {
        TextView view = Objects.requireNonNull(getActivity()).findViewById(R.id.txt_sms_view);
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("smsinbox", view.getText());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
        Snackbar.make(Objects.requireNonNull(getView()), "Copied sms to clipboard", Snackbar.LENGTH_LONG).show();
    }

    private void backupSmsInbox(String mobileNumber) {
        try {
            Log.d(LOG_TAG, String.format("backup sms, %s", mobileNumber));
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
            } else {
                Log.d(LOG_TAG, String.format("backupSmsInbox: Backup up to date, %s", filePath));
                Snackbar.make(Objects.requireNonNull(getView()), String.format("Backup up to date, %s", filePath), Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(Objects.requireNonNull(getView()), "sms backup failed! Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
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

    private String getInputMobileNumber() {
        EditText mobileNumber = Objects.requireNonNull(getActivity()).findViewById(R.id.inp_sms_mobile_number);
        if (mobileNumber.getText().toString().isEmpty()) {
            return ALL.toLowerCase();
        }
        return mobileNumber.getText().toString();
    }

    private List<Sms> getSmsBackup(String mobileNumber) {
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

    private void viewSmsBackupFiles() {
        File appDir = getSmsBackupDir();
        TextView tv = Objects.requireNonNull(getActivity()).findViewById(R.id.txt_sms_view);
        List<String> files = Arrays.asList(Objects.requireNonNull(appDir.list(getJsonFileNameFilter())));
        tv.setText(String.format("%s", files));
        Log.d(LOG_TAG, String.format("viewSmsBackupFiles: number of files: %s", files.size()));
    }

    private List<Sms> searchSms() {
        TextView searchAfter = Objects.requireNonNull(getActivity()).findViewById(R.id.inp_sms_mobile_number);
        List<Sms> smsList = getSmsInbox(null);
        Log.d(LOG_TAG, String.format("searchSms: number of sms: %s, search after: %s", smsList.size(), searchAfter.getText().toString()));
        List<Sms> result = smsList.stream().filter(sms -> sms.getBody().contains(searchAfter.getText().toString())).collect(Collectors.toList());
        TextView view = getActivity().findViewById(R.id.txt_sms_view);
        view.setText(String.format("Search after: %s\n %s", searchAfter.getText().toString(), result));
        return result;
    }

    private void deleteSmsBackupFiles() {
        try {
            File bckDir = getSmsBackupDir();
            if (bckDir.exists()) {
                for (File f : Objects.requireNonNull(bckDir.listFiles(getJsonFileNameFilter()))) {
                    f.delete();
                    Log.d(LOG_TAG, String.format("deleteSmsBackupFiles: Deleted file: %s", f.getName()));
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

    private String getFileContent(String filePath) throws FileNotFoundException {
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while (true) {
            try {
                if ((line = reader.readLine()) == null) {
                    break;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
            }
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sms_clipboard:
                copyToClipboard();
                break;
            case R.id.btn_sms_export:
                List<Sms> inbox = getSmsInbox(getInputMobileNumber());
                RxBus.getInstance().publish(inbox);
                Snackbar.make(Objects.requireNonNull(getView()), String.format("Published sms backup history (%s)", inbox.size()), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_sms_backup:
                backupSmsInbox(getInputMobileNumber());
                break;
            case R.id.btn_sms_chart_view:
                List<Sms> smsList = getSmsBackup(getInputMobileNumber());
                RxBus.getInstance().publish(smsList);
                // Snackbar.make(Objects.requireNonNull(getView()), String.format("Published sms backup history (%s)", smsList.size()), Snackbar.LENGTH_LONG).show();
                // navigate to chart tab
                viewPager.setCurrentItem(TabsPagerAdapter.getTabNumber(R.string.tab_title_chart));
                break;
            case R.id.btn_sms_backup_files_view:
                viewSmsBackupFiles();
                break;
            case R.id.btn_sms_delete_backup_file:
                //deleteSmsBackupFiles();
                DialogFragment newFragment = ConfirmDialogFragment.newInstance("Confirm Delete SMS Backup Files", "Are You Sure?");
                newFragment.show(getChildFragmentManager(), "dialog");
                break;
            case R.id.btn_sms_search:
                RxBus.getInstance().publish(searchSms());
                // Snackbar.make(Objects.requireNonNull(getView()), "Published sms search result", Snackbar.LENGTH_LONG).show();
                viewPager.setCurrentItem(TabsPagerAdapter.getTabNumber(R.string.tab_search_result));
                break;
        }
    }

    @Override
    public void onDialogAction(int actionCode) {
        Log.d(LOG_TAG, String.format("onDialogAction: action: %s", actionCode));
        if (actionCode == DialogActionListener.OK_ACTION) {
            // the user confirmed the operation
            // deleteSmsBackupFiles();
            Snackbar.make(Objects.requireNonNull(getView()), "Deleted sms backup files.", Snackbar.LENGTH_LONG).show();
        }  // dismiss, do nothing, the user canceled the operation
    }
}
