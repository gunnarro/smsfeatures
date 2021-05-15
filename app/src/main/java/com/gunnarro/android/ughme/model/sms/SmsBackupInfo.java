package com.gunnarro.android.ughme.model.sms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Delombok following annotations:
 * Builder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsBackupInfo implements Serializable {

    BackupStatusEnum status;
    String smsBackupFilePath;
    long smsBackupFileSizeBytes;
    long storageFreeSpaceBytes;
    Long lastBackupTime;
    Long fromDateTime;
    Long toDateTime;
    int numberOfSms;
    int numberOfMobileNumbers;

    /**
     * needed by jackson
     */
    public SmsBackupInfo() {
    }

    SmsBackupInfo(BackupStatusEnum status, String smsBackupFilePath, long smsBackupFileSizeBytes, long storageFreeSpaceBytes, Long lastBackupTime, Long fromDateTime, Long toDateTime, int numberOfSms, int numberOfMobileNumbers) {
        this.status = status;
        this.smsBackupFilePath = smsBackupFilePath;
        this.smsBackupFileSizeBytes = smsBackupFileSizeBytes;
        this.storageFreeSpaceBytes = storageFreeSpaceBytes;
        this.lastBackupTime = lastBackupTime;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        this.numberOfSms = numberOfSms;
        this.numberOfMobileNumbers = numberOfMobileNumbers;
    }

    public static SmsBackupInfoBuilder builder() {
        return new SmsBackupInfoBuilder();
    }

    public String getSmsBackupFilePath() {
        return smsBackupFilePath;
    }

    public void setSmsBackupFilePath(String smsBackupFilePath) {
        this.smsBackupFilePath = smsBackupFilePath;
    }

    public long getSmsBackupFileSizeBytes() {
        return smsBackupFileSizeBytes;
    }

    public void setSmsBackupFileSizeBytes(long smsBackupFileSizeBytes) {
        this.smsBackupFileSizeBytes = smsBackupFileSizeBytes;
    }

    public long getStorageFreeSpaceBytes() {
        return storageFreeSpaceBytes;
    }

    public void setStorageFreeSpaceBytes(long storageFreeSpaceBytes) {
        this.storageFreeSpaceBytes = storageFreeSpaceBytes;
    }

    public BackupStatusEnum getStatus() {
        return status;
    }

    public void setStatus(BackupStatusEnum status) {
        this.status = status;
    }

    public Long getLastBackupTime() {
        return lastBackupTime;
    }

    public void setLastBackupTime(Long lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
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

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmsBackupInfo{");
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

    public enum BackupStatusEnum {
        BACKED_UP, NOT_BACKED_UP
    }

    public static class SmsBackupInfoBuilder {
        private BackupStatusEnum status;
        private String smsBackupFilePath;
        private long smsBackupFileSizeBytes;
        private long storageFreeSpaceBytes;
        private Long lastBackupTime;
        private Long fromDateTime;
        private Long toDateTime;
        private int numberOfSms;
        private int numberOfMobileNumbers;

        SmsBackupInfoBuilder() {
        }

        public SmsBackupInfoBuilder status(BackupStatusEnum status) {
            this.status = status;
            return this;
        }

        public SmsBackupInfoBuilder smsBackupFilePath(String smsBackupFilePath) {
            this.smsBackupFilePath = smsBackupFilePath;
            return this;
        }

        public SmsBackupInfoBuilder smsBackupFileSizeBytes(long smsBackupFileSizeBytes) {
            this.smsBackupFileSizeBytes = smsBackupFileSizeBytes;
            return this;
        }

        public SmsBackupInfoBuilder storageFreeSpaceBytes(long storageFreeSpaceBytes) {
            this.storageFreeSpaceBytes = storageFreeSpaceBytes;
            return this;
        }

        public SmsBackupInfoBuilder lastBackupTime(Long lastBackupTime) {
            this.lastBackupTime = lastBackupTime;
            return this;
        }

        public SmsBackupInfoBuilder fromDateTime(Long fromDateTime) {
            this.fromDateTime = fromDateTime;
            return this;
        }

        public SmsBackupInfoBuilder toDateTime(Long toDateTime) {
            this.toDateTime = toDateTime;
            return this;
        }

        public SmsBackupInfoBuilder numberOfSms(int numberOfSms) {
            this.numberOfSms = numberOfSms;
            return this;
        }

        public SmsBackupInfoBuilder numberOfMobileNumbers(int numberOfMobileNumbers) {
            this.numberOfMobileNumbers = numberOfMobileNumbers;
            return this;
        }

        public SmsBackupInfo build() {
            return new SmsBackupInfo(status, smsBackupFilePath, smsBackupFileSizeBytes, storageFreeSpaceBytes, lastBackupTime, fromDateTime, toDateTime, numberOfSms, numberOfMobileNumbers);
        }
    }
}
