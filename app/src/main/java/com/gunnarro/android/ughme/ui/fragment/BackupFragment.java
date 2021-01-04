package com.gunnarro.android.ughme.ui.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.ui.dialog.ConfirmDialogFragment;
import com.gunnarro.android.ughme.ui.dialog.DialogActionListener;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 *
 */
@AndroidEntryPoint
public class BackupFragment extends Fragment implements View.OnClickListener, DialogActionListener {

    private static final String LOG_TAG = BackupFragment.class.getSimpleName();
    public static final String ALL = "all";
    private static final int REQUEST_PERMISSIONS_CODE_READ_SMS = 22;

    private final SmsBackupServiceImpl smsBackupService;

    @Inject
    public BackupFragment(@NonNull SmsBackupServiceImpl smsBackupService) {
        this.smsBackupService = smsBackupService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_backup, container, false);
        view.findViewById(R.id.btn_sms_backup_btn).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_delete_backup_btn).setOnClickListener(this);
        Log.d(LOG_TAG, "onCreateView");
        return view;
    }

    /**
     * Update backup info after view is successfully created
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateSmsBackupInfo(smsBackupService.readSmsBackupMetaData());
    }

    private void updateSmsBackupInfo(SmsBackupInfo info) {
        View view = getView();
        if (info != null) {
            TextView statusView = view.findViewById(R.id.sms_backup_status_value);
            statusView.setText(info.getStatus() != null ? info.getStatus().name() : "");

            TextView backUpDateView = view.findViewById(R.id.sms_backup_date_value);
            backUpDateView.setText(Utility.formatTime(info.getLastBackupTime()));

            TextView filePathView = view.findViewById(R.id.file_path_value);
            filePathView.setText(info.getBackupFilePath());

            TextView fileNameView = view.findViewById(R.id.file_name_value);
            fileNameView.setText(info.getBackupFileName());

            TextView fileSizeView = view.findViewById(R.id.file_size_value);
            fileSizeView.setText(Formatter.formatFileSize(getActivity().getApplicationContext(), info.getSmsBackupFileSizeBytes()));

            TextView storageFreeSpaceView = view.findViewById(R.id.storage_free_space_value);
            storageFreeSpaceView.setText(Formatter.formatFileSize(getActivity().getApplicationContext(), info.getStorageFreeSpaceBytes()));

            TextView smsFromDateView = view.findViewById(R.id.sms_from_date_value);
            smsFromDateView.setText(Utility.formatTime(info.getFromDateTime()));

            TextView smsToDateView = view.findViewById(R.id.sms_to_date_value);
            smsToDateView.setText(Utility.formatTime(info.getToDateTime()));

            TextView smsNumberView = view.findViewById(R.id.number_of_sms_value);
            smsNumberView.setText(String.format("%s", info.getNumberOfSms()));

            TextView mobileView = view.findViewById(R.id.number_of_mobile_nr_value);
            mobileView.setText(String.format("%s", info.getNumberOfMobileNumbers()));

            Log.d(LOG_TAG, String.format("updated view with sms backup metadata. %s ", info));
        } else {
            Log.e(LOG_TAG, String.format("something failed updating sms backup metadata view,  %s ", info));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_sms_backup_btn) {
            Dialog d = buildProgressDialog();
            d.show();
            smsBackupService.backupSmsInbox();
            SmsBackupInfo info = smsBackupService.readSmsBackupMetaData();
            updateSmsBackupInfo(info);
            d.dismiss();
        } else if (id == R.id.btn_sms_delete_backup_btn) {
            DialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Confirm Delete SMS Backup Files", "Are You Sure?");
            confirmDialog.show(getChildFragmentManager(), "dialog");
        }
    }

    private Dialog buildProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(R.layout.dlg_progress);
        Dialog progressDialog = builder.create();
        progressDialog.setTitle("Backup sms");
        progressDialog.setCancelable(true);
        return progressDialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE_READ_SMS) {
            if (permissions[0].equals(Manifest.permission.READ_SMS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "sms permission granted");
            }
        }
    }

    @Override
    public void onDialogAction(int actionCode) {
        if (actionCode == DialogActionListener.OK_ACTION) {
            // the user confirmed the operation
            smsBackupService.clearSmsBackupFile();
            SmsBackupInfo info = smsBackupService.readSmsBackupMetaData();
            updateSmsBackupInfo(info);
            Snackbar.make(requireView(), "Deleted sms backup files.", Snackbar.LENGTH_LONG).show();
        } else {
            // dismiss, do nothing, the user canceled the operation
            Log.d(LOG_TAG, "delete sms backup file action cancelled by user");
        }
    }
}
