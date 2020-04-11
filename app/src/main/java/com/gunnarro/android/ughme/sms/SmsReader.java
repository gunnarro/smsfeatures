package com.gunnarro.android.ughme.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SmsReader {

    private static final String CONTENT_SMS_INBOX = "content://sms/inbox";
    private static final String CONTENT_SMS_OUTBOX = "content://sms/inbox";
    private static final String ID = "_id";
    private static final String ADDRESS = "address";
    private static final String PERSON = "person";
    private static final String DATE = "date";
    private static final String READ = "read";
    private static final String STATUS = "status";
    private static final String TYPE = "type";
    private static final String BODY = "body";
    private static final String SEEN = "seen";
    private static final String SORT_ORDER = "date DESC";

    private static final int MESSAGE_TYPE_INBOX = 1;
    private static final int MESSAGE_TYPE_SENT = 2;

    private static final int MESSAGE_IS_NOT_READ = 0;
    private static final int MESSAGE_IS_READ = 1;

    private static final int MESSAGE_IS_NOT_SEEN = 0;
    private static final int MESSAGE_IS_SEEN = 1;

    private final Context context;

    public SmsReader(Context context) {
        this.context = context;
    }

    /**
     * Inbox columns names: _id, thread_id address person date protocol read
     * status type subject body service_center locked error_code seen
     *
     * @return
     */
    public List<Sms> getSMSInbox(boolean isOnlyUnread, String filterByNumber) {
        List<Sms> smsInbox = new ArrayList<>();
        Uri smsUri = Uri.parse(CONTENT_SMS_INBOX);
        String[] projection = new String[]{ID, ADDRESS, BODY, DATE, READ, STATUS, SEEN, TYPE, "datetime(julianday(date)) AS group_by"};
        Cursor cursor = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss", Locale.ENGLISH);
        try {
            cursor = context.getContentResolver().query(smsUri
                    , projection
                    , null // selection
                    , null // selectionArgs
                    , SORT_ORDER); // sortOrder

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int read = cursor.getInt(cursor.getColumnIndex(READ));
                    if (isOnlyUnread) {
                        if (read == MESSAGE_IS_READ) {
                            // Skip read sms
                            continue;
                        }
                    }

                    String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    Sms sms = Sms.builder()
                            .setIsRead(read == MESSAGE_IS_READ)
                            .setAddress(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)))
                            .setStatus(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS)))
                            .setSeen(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.SEEN)))
                            .setType(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)))
                            .setBody(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)))
                            .setDate(Long.parseLong(date))
                            .setCount(1)
                            .build();
                    // Check if we should filter
                    if (filterByNumber != null && !filterByNumber.isEmpty() && !filterByNumber.equalsIgnoreCase("all") && sms.getAddress() != null) {
                        if (sms.getAddress().contains(filterByNumber)) {
                            smsInbox.add(sms);
                        }
                    } else {
                        // no filter, add all
                        smsInbox.add(sms);
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return smsInbox;
    }

    /**
     * SELECT SUM(Price) as Price, strftime('%m', myDateCol) as Month FROM
     * myTable GROUP BY strftime('%m', myDateCol)
     * <p>
     * <p>
     * strftime function: %d day of month: 00 %f fractional seconds: SS.SSS %H
     * hour: 00-24 %j day of year: 001-366 %J Julian day number %m month: 01-12
     * %M minute: 00-59 %s seconds since 1970-01-01 %S seconds: 00-59 %w day of
     * week 0-6 with Sunday==0 %W week of year: 00-53 %Y year: 0000-9999 %% %
     *
     * @return
     */
    public List<Sms> getSMSOutboxGroupBy(String period, boolean isGroupByAddress) {
        String groupByPeriod = "%Y";
        if (period.equalsIgnoreCase("year")) {
            // groupByPeriod = "%Y";
            groupByPeriod = "yyyy";
        } else if (period.equalsIgnoreCase("month")) {
            // groupByPeriod = "%Y-%m";
            groupByPeriod = "yyyy-MM";
        } else if (period.equalsIgnoreCase("day")) {
            // groupByPeriod = "%Y-%m-%d";
            groupByPeriod = "yyyy-MM-dd";
        } else if (period.equalsIgnoreCase("week")) {
            // groupByPeriod = "%W";
            groupByPeriod = "yyyy w";
        }
        // String[] projection = new String[]{"strftime('" + groupByPeriod +
        // "', dateTime(julianday(date))) AS group_by",
        // "count(date) AS number_of_sms"};
        // String selection = "type == 1) GROUP BY (strftime('" + groupByPeriod
        // + "', dateTime(julianday(date)))";
        String[] projection = new String[]{DATE, TYPE};
        String selection = null;

        if (isGroupByAddress) {
            projection = new String[]{"address AS group_by", "count(address) AS number_of_sms"};
            selection = "type == 1) GROUP BY (address";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(groupByPeriod, Locale.ENGLISH);
        Map<String, Sms> smsInboxMap = new HashMap<>();
        Uri smsUri = Uri.parse(CONTENT_SMS_INBOX);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(smsUri, projection, selection // selection
                    , null // selectionArgs
                    , SORT_ORDER); // sortOrder

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String p = cursor.getString(cursor.getColumnIndex(DATE));
                    int msgType = cursor.getInt(cursor.getColumnIndex(TYPE));
                    // int numberOfSms =
                    // cursor.getInt(cursor.getColumnIndex("number_of_sms"));
                    String datePeriod = formatter.format(new Date(Long.parseLong(p)));
                    Sms sms = smsInboxMap.get(datePeriod);
                    if (sms == null) {
                        sms = Sms.builder().setPeriod(datePeriod).setCount(0).build();
                        smsInboxMap.put(sms.getPeriod(), sms);
                    }
                    if (msgType == MESSAGE_TYPE_INBOX) {
                        //  sms.increaseNumberOfReceived();
                    } else if (msgType == MESSAGE_TYPE_SENT) {
                        //   sms.increaseNumberOfSent();
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // return smsInbox;
        return new ArrayList<>(smsInboxMap.values());
    }
}
