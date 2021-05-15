package com.gunnarro.android.ughme.model.sms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sms implements Serializable, Comparable<Sms> {
    private boolean isRead;
    private String status;
    private String seen;
    private Long timeMs;
    private String address;
    private String body;
    private String contactName;
    private String type;
    private String period;
    private int numberOfReceived;
    private int numberOfSent;
    private int numberOfBlocked;
    private Integer count;

    /**
     * needed by jackson
     */
    public Sms() {
    }

    Sms(boolean isRead, String status, String seen, Long timeMs, String address, String body, String contactName, String type, String period, int numberOfReceived, int numberOfSent, int numberOfBlocked, Integer count) {
        this.isRead = isRead;
        this.status = status;
        this.seen = seen;
        this.timeMs = timeMs;
        this.address = address;
        this.body = body;
        this.contactName = contactName;
        this.type = type;
        this.period = period;
        this.numberOfReceived = numberOfReceived;
        this.numberOfSent = numberOfSent;
        this.numberOfBlocked = numberOfBlocked;
        this.count = count;
    }

    public static SmsBuilder builder() {
        return new SmsBuilder();
    }

    @Override
    public int compareTo(Sms sms) {
        return this.count.compareTo(sms.count);
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeen() {
        return this.seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public Long getTimeMs() {
        return this.timeMs;
    }

    public void setTimeMs(Long timeMs) {
        this.timeMs = timeMs;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContactName() {
        return this.contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getNumberOfReceived() {
        return this.numberOfReceived;
    }

    public void setNumberOfReceived(int numberOfReceived) {
        this.numberOfReceived = numberOfReceived;
    }

    public int getNumberOfSent() {
        return this.numberOfSent;
    }

    public void setNumberOfSent(int numberOfSent) {
        this.numberOfSent = numberOfSent;
    }

    public int getNumberOfBlocked() {
        return this.numberOfBlocked;
    }

    public void setNumberOfBlocked(int numberOfBlocked) {
        this.numberOfBlocked = numberOfBlocked;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return contactName != null ? contactName : address;
    }

    @NotNull
    public String toString() {
        return "Sms(isRead=" + this.isRead() + ", status=" + this.getStatus() + ", seen=" + this.getSeen() + ", timeMs=" + this.getTimeMs() + ", address=" + this.getAddress() + ", body=" + this.getBody() + ", contactName=" + this.getContactName() + ", type=" + this.getType() + ", period=" + this.getPeriod() + ", numberOfReceived=" + this.getNumberOfReceived() + ", numberOfSent=" + this.getNumberOfSent() + ", numberOfBlocked=" + this.getNumberOfBlocked() + ", count=" + this.getCount() + ")";
    }

    public static class SmsBuilder {
        private boolean isRead;
        private String status;
        private String seen;
        private Long timeMs;
        private String address;
        private String body;
        private String contactName;
        private String type;
        private String period;
        private int numberOfReceived;
        private int numberOfSent;
        private int numberOfBlocked;
        private Integer count;

        SmsBuilder() {
        }

        public SmsBuilder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public SmsBuilder status(String status) {
            this.status = status;
            return this;
        }

        public SmsBuilder seen(String seen) {
            this.seen = seen;
            return this;
        }

        public SmsBuilder timeMs(Long timeMs) {
            this.timeMs = timeMs;
            return this;
        }

        public SmsBuilder address(String address) {
            this.address = address;
            return this;
        }

        public SmsBuilder body(String body) {
            this.body = body;
            return this;
        }

        public SmsBuilder contactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public SmsBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SmsBuilder period(String period) {
            this.period = period;
            return this;
        }

        public SmsBuilder numberOfReceived(int numberOfReceived) {
            this.numberOfReceived = numberOfReceived;
            return this;
        }

        public SmsBuilder numberOfSent(int numberOfSent) {
            this.numberOfSent = numberOfSent;
            return this;
        }

        public SmsBuilder numberOfBlocked(int numberOfBlocked) {
            this.numberOfBlocked = numberOfBlocked;
            return this;
        }

        public SmsBuilder count(Integer count) {
            this.count = count;
            return this;
        }

        public Sms build() {
            return new Sms(isRead, status, seen, timeMs, address, body, contactName, type, period, numberOfReceived, numberOfSent, numberOfBlocked, count);
        }

    }
}
