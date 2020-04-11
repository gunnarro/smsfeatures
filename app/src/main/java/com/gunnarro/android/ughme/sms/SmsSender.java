package com.gunnarro.android.ughme.sms;

import android.telephony.SmsManager;
import android.util.Log;

public class SmsSender {

    /**
     * Sends an SMS message to another device, without any feedback
     */
    public void sendSMS(String phoneNumber, String message) {
        try {
            Log.d("DEBUG", "Send sms to " + phoneNumber);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR send sms", e.getMessage());
        }
    }
}
