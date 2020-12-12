package com.gunnarro.android.ughme.sms;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class SmsBackupInfo implements Serializable {

    String smsBackupFilePath;
    long fromDateTime;
    long toDateTime;
    int numberOfSms;
    int numberOfMobileNumbers;

    public String getSmsBackupFilePath() {
        return smsBackupFilePath;
    }

    public void setSmsBackupFilePath(String smsBackupFilePath) {
        this.smsBackupFilePath = smsBackupFilePath;
    }

    public long getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(long fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public long getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(long toDateTime) {
        this.toDateTime = toDateTime;
    }

    public int getNumberOfSms() {
        return numberOfSms;
    }

    public void setNumberOfSms(int numberOfSms) {
        this.numberOfSms = numberOfSms;
    }

    public int getNumberOfMobileNumbers() {
        return numberOfMobileNumbers;
    }

    public void setNumberOfMobileNumbers(int numberOfMobileNumbers) {
        this.numberOfMobileNumbers = numberOfMobileNumbers;
    }

    private String formatDate(long timeMs) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(timeMs));

    }
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SmsBackupInfo{");
        sb.append("smsBackupFilePath='").append(smsBackupFilePath).append('\'');
        sb.append(", fromDateTime=").append(formatDate(fromDateTime));
        sb.append(", toDateTime=").append(formatDate(toDateTime));
        sb.append(", numberOfSms=").append(numberOfSms);
        sb.append(", numberOfMobileNumbers=").append(numberOfMobileNumbers);
        sb.append('}');
        return sb.toString();
    }
}
