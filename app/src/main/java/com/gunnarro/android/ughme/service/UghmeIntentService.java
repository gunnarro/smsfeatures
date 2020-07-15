package com.gunnarro.android.ughme.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.gunnarro.android.ughme.AppPreferences;
import com.gunnarro.android.ughme.ListAppPreferencesImpl;
import com.gunnarro.android.ughme.location.MyLocationManager;
import com.gunnarro.android.ughme.location.Position;
import com.gunnarro.android.ughme.mail.MailSender;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.sms.SmsHandler;
import com.gunnarro.android.ughme.sms.SmsMsg;
import com.gunnarro.android.ughme.sms.SmsReader;
import com.gunnarro.android.ughme.sms.SmsSender;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UghmeIntentService extends IntentService {

    private static final String TAG = UghmeIntentService.class.getName();
    private MediaPlayer player;
    private Handler handler;

    /**
     * Default constructor
     */
    public UghmeIntentService() {
        super(TAG);
    }

    private void log(final String msg, boolean isToast) {
        Log.i(TAG, msg);
        if (isToast) {
            handler.post(() -> Toast.makeText(UghmeIntentService.this, msg, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            log(TAG + " onHandleIntent", false);
            if (intent.getExtras() != null) {
                if (intent.getExtras().getString(SmsHandler.KEY_MOBILE_NUMBER) == null) {
                    log(TAG + " missing mobile number in extras", false);
                    return;
                }
                handleReceivedSMS(new SmsMsg(intent.getExtras().getString(SmsHandler.KEY_MOBILE_NUMBER), intent.getExtras().getString(SmsHandler.KEY_SMS_MSG)),
                        this);
            } else {
                log(TAG + " extras is null !", false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        log(TAG + "Created", false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log(TAG + " Stopped", false);
        handler = null;
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    private void alertUser(String msg) {
        log(msg, true);
        // player = MediaPlayer.create(this, R.raw.starwars);
        // player.setLooping(false); // Set looping
        // player.start();
    }

    private void handleReceivedSMS(SmsMsg receivedSMSMsg, Context context) {
        if (receivedSMSMsg.isTraceSMS()) {
            handleTraceSMS(receivedSMSMsg, context);
        } else if (receivedSMSMsg.isForwardSMS()) {
            handleForwardSMS(receivedSMSMsg, context);
        } else {
            log("Ordinary sms: " + receivedSMSMsg.toString(), false);
        }
    }

    /**
     *
     */
    private void handleTraceSMS(SmsMsg receivedSMSMsg, Context context) {
        //if (new ListAppPreferencesImpl(context, AppPreferences.AUTHENTICATED_USERS).listContains(receivedSMSMsg.getToMobilePhoneNumber())) {
        log(receivedSMSMsg.getToMobilePhoneNumber() + " is authenticated", false);
        // give the mobile owner a signal that he has been traced.
        // get the mobile location if available
        Position myPosition = null;
        try {
            myPosition = MyLocationManager.getLocationLastKnown(context, receivedSMSMsg.getToMobilePhoneNumber());
        } catch (Exception e) {
            Log.e("sms", Objects.requireNonNull(e.getMessage()));
        }
        String from = lookupContactName(receivedSMSMsg.getToMobilePhoneNumber());
        if (from == null) {
            from = receivedSMSMsg.getToMobilePhoneNumber();
        }
        alertUser("Sent trace sms to: " + from + ", url: " + myPosition);
        SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy hh:ss:mm", Locale.getDefault());
        StringBuilder msg = new StringBuilder();
        if (myPosition != null) {
            msg.append("time: ").append(sd.format(myPosition.getTime())).append("\n");
            msg.append("mobile:").append(myPosition.getMobileNumber()).append("\n");
            msg.append(myPosition.createGoogleMapUrl());
        } else {
            msg.append(String.format("Location not found! mobile=%s", receivedSMSMsg.getToMobilePhoneNumber()));
        }
        new SmsSender().sendSMS(receivedSMSMsg.getToMobilePhoneNumber(), msg.toString());
    }

    private void handleForwardSMS(SmsMsg receivedSMSMsg, Context context) {
        if (new ListAppPreferencesImpl(context, AppPreferences.AUTHENTICATED_USERS).listContains(receivedSMSMsg.getToMobilePhoneNumber())) {
            MailSender mailSender = new MailSender("username@gmail.com", "userpass");
            try {
                SmsReader smsReader = new SmsReader(context);
                List<Sms> unreadSms = smsReader.getSMSInbox(true, "");
                String missesCalls = "";//CallRegister.getMissedCalls(context);
                StringBuilder msg = new StringBuilder();
                msg.append("-------------------------------------------\n");
                msg.append("Call and sms log for ").append(getDevicePhoneNumber()).append("\n");
                msg.append("-------------------------------------------\n");
                msg.append("Misses calls:\n");
                msg.append("-------------------------------------------\n");
                msg.append(missesCalls).append("\n\n");
                msg.append("Unread sms:\n");
                msg.append("-------------------------------------------\n");
                for (Sms sms : unreadSms) {
                    msg.append(sms.toString()).append("\n");
                }
                mailSender.sendMail("Call and SMS log", msg.toString(), "gunnar.ronneberg@gmail.com", "gunnar.ronneberg@gmail.com");
                alertUser("Forwarded call log and sms inbox to email address gunnar.ronneberg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log("Ingnore this request, the requester was not authenticated to use the sms forward service, mobile number: "
                    + receivedSMSMsg.getToMobilePhoneNumber(), true);
        }
    }

    private String lookupContactName(String mobileNumber) {
        String contactDisplayName = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(mobileNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        try {
            // Query the filter URI
            Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactDisplayName = cursor.getString(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("sms", Objects.requireNonNull(e.getMessage()));
        }
        return contactDisplayName;
    }

    private boolean isAuthenticated(String mobilePhoneNumber) {
        return mobilePhoneNumber.equals(getDevicePhoneNumber());
    }

    private String getDevicePhoneNumber() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "na";
        }
        return tm != null ? tm.getLine1Number() : null;
    }
}
