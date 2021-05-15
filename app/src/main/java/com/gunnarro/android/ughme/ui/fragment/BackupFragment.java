package com.gunnarro.android.ughme.ui.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.sms.SmsBackupInfo;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.BackupEvent;
import com.gunnarro.android.ughme.service.SmsBackupTask;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.ui.dialog.ConfirmDialogFragment;
import com.gunnarro.android.ughme.ui.dialog.DialogActionListener;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;


@AndroidEntryPoint
public class BackupFragment extends Fragment implements View.OnClickListener, DialogActionListener {

    public static final String ALL = "all";

    @Inject
    SmsBackupTask backupTask;

    @Inject
    SmsBackupServiceImpl smsBackupService;

    private Dialog progressDialog;

    // Check permission
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    /*
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                } else {
                    Log.d(Utility.buildTag(ActivityResultContracts.class, "RequestPermission"), "permission not granted!");
                    // explain for user why this permission is needed
                    return;
                }
            });
*/
    @Inject
    public BackupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(Utility.buildTag(getClass(), "onCreate"), "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_backup, container, false);
        view.findViewById(R.id.btn_sms_backup_save).setOnClickListener(this);
        view.findViewById(R.id.btn_sms_backup_delete).setOnClickListener(this);
        RxBus.getInstance().listen().observeOn(AndroidSchedulers.mainThread()).subscribe(getInputObserver());
        Log.d(Utility.buildTag(getClass(), "onCreateView"), "");
        return view;
    }

    /**
     * Update backup info after view is successfully create
     */
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateSmsBackupInfo(getView(), smsBackupService.readSmsBackupMetaData());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateSmsBackupInfo(View view, SmsBackupInfo info) {
        if (view != null && info != null) {
            TextView statusView = view.findViewById(R.id.sms_backup_status_value);
            statusView.setText(info.getStatus() != null ? info.getStatus().name() : "");

            TextView backUpDateView = view.findViewById(R.id.sms_backup_date_value);
            backUpDateView.setText(Utility.formatTime(info.getLastBackupTime()));

            TextView downloadPathView = view.findViewById(R.id.download_folder_path_value);
            downloadPathView.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());

            TextView filePathView = view.findViewById(R.id.file_path_value);
            filePathView.setText(info.getBackupFilePath());

            TextView fileNameView = view.findViewById(R.id.file_name_value);
            fileNameView.setText(info.getBackupFileName());

            TextView fileSizeView = view.findViewById(R.id.file_size_value);
            fileSizeView.setText(Formatter.formatFileSize(requireActivity().getApplicationContext(), info.getSmsBackupFileSizeBytes()));

            TextView storageFreeSpaceView = view.findViewById(R.id.storage_free_space_value);
            storageFreeSpaceView.setText(Formatter.formatFileSize(requireActivity().getApplicationContext(), info.getStorageFreeSpaceBytes()));

            TextView smsFromDateView = view.findViewById(R.id.sms_from_date_value);
            smsFromDateView.setText(Utility.formatTime(info.getFromDateTime()));

            TextView smsToDateView = view.findViewById(R.id.sms_to_date_value);
            smsToDateView.setText(Utility.formatTime(info.getToDateTime()));

            TextView smsNumberView = view.findViewById(R.id.number_of_sms_value);
            smsNumberView.setText(String.format("%s", info.getNumberOfSms()));

            TextView mobileView = view.findViewById(R.id.number_of_mobile_nr_value);
            mobileView.setText(String.format("%s", info.getNumberOfMobileNumbers()));

            Log.d(Utility.buildTag(getClass(), "updateSmsBackupInfo"), String.format("updated view with sms backup metadata. %s ", info));
        } else {
            Log.e(Utility.buildTag(getClass(), "updateSmsBackupInfo"), "ERROR: view or info is null!");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onClick(View view) {
        // ask every time
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // You have not been granted access, ask for permission now.
            //requestPermissionLauncher.launch(Manifest.permission.ACCESS_MEDIA_LOCATION);
        } else {
            Log.d(Utility.buildTag(getClass(), "onClick"), "backup button, permission granted");
        }

        int id = view.getId();
        if (id == R.id.btn_sms_backup_save) {
            startBackupSms();
            //sendEmail("test send epost fra android app");
        } else if (id == R.id.btn_sms_backup_delete) {
            DialogFragment confirmDialog = ConfirmDialogFragment.newInstance(getString(R.string.msg_delete_sms_backup), getString(R.string.msg_confirm_delete));
            confirmDialog.show(getChildFragmentManager(), "dialog");
        }
    }

    private void startBackupSms() {
        progressDialog = buildProgressDialog();
        progressDialog.show();
        // start background task for building word cloud, which may take som time, based on number of sms
        CheckBox saveExternal = requireActivity().findViewById(R.id.check_save_external);
        backupTask.backupSms(saveExternal.isChecked());
    }

    private Dialog buildProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(R.layout.dlg_progress);
        Dialog progressDialog = builder.create();
        progressDialog.setTitle("Backup sms");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    @Override
    public void onDialogAction(int actionCode) {
        if (actionCode == DialogActionListener.OK_ACTION) {
            // the user confirmed the operation
            smsBackupService.deleteSmsBackupFile();
            updateSmsBackupInfo(getView(), smsBackupService.readSmsBackupMetaData());
            Snackbar.make(requireView(), "Deleted sms backup files.", Snackbar.LENGTH_LONG).show();
        } else {
            // dismiss, do nothing, the user canceled the operation
            Log.d(Utility.buildTag(getClass(), "onDialogAction"), "delete sms backup file action cancelled by user");
        }
    }

    // *********************************************************************************************
    // Get RxJava input observer instance
    // *********************************************************************************************
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(".getInputObserver.onSubscribe", "getInputObserver.onSubscribe");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                //Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof BackupEvent) {
                    BackupEvent event = (BackupEvent) obj;
                    Log.d(Utility.buildTag(getClass(), "onNext"), String.format("thread=%s event= %s", Thread.currentThread().getName(), event.toString()));
                    if (event.isBackupFinished()) {
                        updateSmsBackupInfo(getView(), smsBackupService.readSmsBackupMetaData());
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(Utility.buildTag(getClass(), "onError"), String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(Utility.buildTag(getClass(), "onComplete"), "");
            }
        };
    }
}
