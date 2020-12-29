package com.gunnarro.android.ughme.model.sms;

import java.io.Serializable;

public class SmsBackupInfo implements Serializable {

    public enum BackupStatusEnum {
        BACKED_UP, NOT_BACKED_UP
    }

    BackupStatusEnum status = BackupStatusEnum.NOT_BACKED_UP;
    String smsBackupFilePath;
    Long lastBackupTime;
    Long fromDateTime;
    Long toDateTime;
    int numberOfSms;
    int numberOfMobileNumbers;

    public String getSmsBackupFilePath() {
        return smsBackupFilePath;
    }

    public void setSmsBackupFilePath(String smsBackupFilePath) {
        this.smsBackupFilePath = smsBackupFilePath;
    }

    public void setStatus(BackupStatusEnum status) {
        this.status = status;
    }

    public BackupStatusEnum getStatus() {
        return status;
    }

    public void setLastBackupTime(Long lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }

    public Long getLastBackupTime() {
        return lastBackupTime;
    }


    public Long getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(Long fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public Long getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(Long toDateTime) {
        this.toDateTime = toDateTime;
    }

    public Integer getNumberOfSms() {
        return numberOfSms;
    }

    public void setNumberOfSms(int numberOfSms) {
        this.numberOfSms = numberOfSms;
    }

    public Integer getNumberOfMobileNumbers() {
        return numberOfMobileNumbers;
    }

    public void setNumberOfMobileNumbers(int numberOfMobileNumbers) {
        this.numberOfMobileNumbers = numberOfMobileNumbers;
    }

    public String getBackupFileName() {
        if (smsBackupFilePath != null && !smsBackupFilePath.isEmpty()) {
            return smsBackupFilePath.substring(smsBackupFilePath.lastIndexOf("/") + 1);
        }
        return null;
    }

    public String getBackupFilePath() {
        if (smsBackupFilePath != null && !smsBackupFilePath.isEmpty()) {
            return smsBackupFilePath.substring(0, smsBackupFilePath.lastIndexOf("/") - 1);
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SmsBackupInfo{");
        sb.append("status=").append(status);
        sb.append(", smsBackupFilePath='").append(smsBackupFilePath).append('\'');
        sb.append(", lastBackupTime=").append(lastBackupTime);
        sb.append(", fromDateTime=").append(fromDateTime);
        sb.append(", toDateTime=").append(toDateTime);
        sb.append(", numberOfSms=").append(numberOfSms);
        sb.append(", numberOfMobileNumbers=").append(numberOfMobileNumbers);
        sb.append('}');
        return sb.toString();
    }
}
