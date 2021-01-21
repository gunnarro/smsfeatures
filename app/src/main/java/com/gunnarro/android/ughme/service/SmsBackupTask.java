package com.gunnarro.android.ughme.service;

import android.util.Log;

import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.BackupEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Run the word cloud build as a background task
 */
public class SmsBackupTask {

    private static final String TAG = SmsBackupTask.class.getSimpleName();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    final SmsBackupServiceImpl smsBackupService;

    @Inject
    public SmsBackupTask(SmsBackupServiceImpl smsBackupService) {
        this.smsBackupService = smsBackupService;
    }

    public void backupSms() {
        Runnable backupSmsRunnable = () -> {
            try {
                long startTimeMs = System.currentTimeMillis();
                smsBackupService.backupSmsInbox();
                // when finished publish result so fragment can pick up the word list
                RxBus.getInstance().publish(
                        BackupEvent.builder()
                                .eventType(BackupEvent.BackupEventEventTypeEnum.BACKUP_FINISHED)
                                .build());
                Log.i(TAG, String.format("SmsBackupTask finished, execution time=%s ms, tread: %s", (System.currentTimeMillis() - startTimeMs), Thread.currentThread().getName()));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        };
        executor.execute(backupSmsRunnable);
    }
}


