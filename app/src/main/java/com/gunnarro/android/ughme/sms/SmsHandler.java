package com.gunnarro.android.ughme.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.gunnarro.android.ughme.AppPreferences;
import com.gunnarro.android.ughme.ListAppPreferencesImpl;
import com.gunnarro.android.ughme.service.UghmeIntentService;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SmsHandler extends BroadcastReceiver {

    public final static String KEY_SMS_MSG = "message";
    public final static String KEY_MOBILE_NUMBER = "mobilenumber";
    private static final String PDUS = "pdus";

    private static String createLogTag(Class<?> clazz) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH).format(new Date()) + " " + clazz.getSimpleName();
    }

    /**
     * A PDU is a "protocol description unit", which is the industry format for
     * an SMS message. because SMSMessage reads/writes them you shouldn't need
     * to disect them. A large message might be broken into many, which is why
     * it is an array of objects.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(createLogTag(this.getClass()), "Handle incoming sms...");
        if (intent != null && intent.getExtras() != null) {
            if (Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                handleSMS(context, intent.getExtras(), Telephony.Sms.Intents.getMessagesFromIntent(intent));
            } else {
                Log.i(createLogTag(this.getClass()), "This was not an sms: " + intent.getAction());
            }
        }
    }

    private void handleSMS(Context context, Bundle bundle, SmsMessage[] smsMesssages) {
        if (bundle == null || bundle.get(PDUS) == null) {
            return;
        }

        for (SmsMessage smsMsg : smsMesssages) {
            try {
                if (isBlackListed(context, smsMsg.getOriginatingAddress())) {
                    Log.i(createLogTag(this.getClass()), "blacklisted, rejected sms from: " + smsMsg.getOriginatingAddress());
                    super.abortBroadcast();
                }
                Intent intent = new Intent(context, UghmeIntentService.class);
                intent.putExtra(KEY_MOBILE_NUMBER, smsMsg.getOriginatingAddress());
                intent.putExtra(KEY_SMS_MSG, smsMsg.getMessageBody());
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isBlackListed(Context context, String phoneNumber) {
        return new ListAppPreferencesImpl(context, AppPreferences.SMS_BLACK_LIST).listContains(phoneNumber);
    }
}
