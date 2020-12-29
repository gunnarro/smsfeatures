package com.gunnarro.android.ughme.model.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import com.gunnarro.android.ughme.ui.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SmsReader {

    private static final String CONTENT_SMS_INBOX = "content://sms/inbox";
    private static final String CONTENT_SMS_OUTBOX = "content://sms/inbox";
    private static final String ID = "_id";
    private static final String SORT_ORDER = Telephony.Sms.DATE + " DESC";

    private static final int MESSAGE_TYPE_ALL = 0;
    private static final int MESSAGE_TYPE_INBOX = 1;
    private static final int MESSAGE_TYPE_SENT = 2;

    private static final int MESSAGE_IS_NOT_READ = 0;
    private static final int MESSAGE_IS_READ = 1;

    private static final int MESSAGE_IS_NOT_SEEN = 0;
    private static final int MESSAGE_IS_SEEN = 1;

    public static final String SMS_ALL = "all";

    private static final Context context = MainActivity.getAppContext();

    /**
     * Inbox columns names: _id, thread_id address person date protocol read
     * status type subject body service_center locked error_code seen
     */
    public static List<Sms> getSMSInbox(boolean isOnlyUnread, String filterByNumber, Long filterByTimeMs) {
        Log.d("SmsReader", String.format("read sms inbox, filterByNumber: %s, filterByTime: %s", filterByNumber, filterByTimeMs != null ? new Date(filterByTimeMs) : null));
        List<Sms> smsInbox = new ArrayList<>();
        String[] projection = new String[]{ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.READ, Telephony.Sms.STATUS, Telephony.Sms.SEEN, Telephony.Sms.TYPE, "datetime(julianday(date)) AS group_by"};
        try (Cursor cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI
                , projection
                , null
                , null
                , SORT_ORDER)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ));
                    if (isOnlyUnread) {
                        if (read == MESSAGE_IS_READ) {
                            // Skip read sms
                            Log.d("SmsReader", String.format("skip sms, ead: %s", read));
                            continue;
                        }
                    }

                    // check if filter by date and time
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    long smsTimeMs = Long.parseLong(date);
                    if (filterByTimeMs != null) {
                        if (smsTimeMs <= filterByTimeMs) {
                            // skip this sms
                            Log.d("SmsReader", String.format("skip sms, date: %s", new Date(smsTimeMs)));
                            continue;
                        }
                    }

                    // Check if we should filter by number
                    String mobileNumber = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    if (filterByNumber != null && !filterByNumber.isEmpty() && !filterByNumber.equalsIgnoreCase(SMS_ALL) && mobileNumber != null) {
                        if (!mobileNumber.equals(filterByNumber)) {
                            // skip this sms
                            Log.d("SmsReader", String.format("skip sms, number: %s", mobileNumber));
                            continue;
                        }
                    }

                    // passed al filters crate and add sms to list
                    Sms sms = Sms.builder()
                            .setIsRead(read == MESSAGE_IS_READ)
                            .setAddress(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)))
                            .setContactName(lookupContactName(mobileNumber))
                            .setStatus(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS)))
                            .setSeen(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.SEEN)))
                            .setType(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)))
                            .setBody(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)))
                            .setDate(smsTimeMs)
                            .setNumberOfReceived(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)).equals("1") ? 1 : 0)
                            .setNumberOfSent(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)).equals("2") ? 1 : 0)
                            .setCount(1)
                            .build();

                    Log.d("SmsReader", String.format("add sms, number: %s, date: %s", mobileNumber, new Date(smsTimeMs)));
                    smsInbox.add(sms);
                } while (cursor.moveToNext());
            }
        }
        return smsInbox;
    }


    private static String lookupContactName(String mobileNumber) {
        String contactDisplayName = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(mobileNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        try {
            // Query the filter URI
            Cursor cursor = context.getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
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
}
