package com.gunnarro.android.ughme.model.sms;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Sms implements Serializable, Comparable<Sms> {
    @Expose(serialize = false, deserialize = false)
    private final boolean isRead;
    @Expose(serialize = false, deserialize = false)
    private final String status;
    @Expose(serialize = false, deserialize = false)
    private final String seen;
    private final Long timeMs;
    private final String address;
    private final String body;
    private final String contactName;
    @Expose(serialize = false, deserialize = false)
    private final String type;
    @Expose(serialize = false, deserialize = false)
    private final String period;
    private final int numberOfReceived;
    private final int numberOfSent;
    private final int numberOfBlocked;
    private final Integer count;

    private Sms(Builder builder) {
        this.isRead = builder.isRead;
        this.status = builder.status;
        this.seen = builder.seen;
        this.timeMs = builder.timeMs;
        this.address = builder.address;
        this.body = builder.body;
        this.type = builder.type;
        this.period = builder.period;
        this.numberOfReceived = builder.numberOfReceived;
        this.numberOfSent = builder.numberOfSent;
        this.numberOfBlocked = builder.numberOfBlocked;
        this.count = builder.count;
        this.contactName = builder.contactName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean getIsRead() {
        return isRead;
    }

    public String getStatus() {
        return status;
    }

    public String getSeen() {
        return seen;
    }

    public Long getTimeMs() {
        return timeMs;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }

    public String getPeriod() {
        return period;
    }

    public int getNumberOfReceived() {
        return numberOfReceived;
    }

    public int getNumberOfSent() {
        return numberOfSent;
    }

    public int getNumberOfBlocked() {
        return numberOfBlocked;
    }

    public Integer getCount() {
        return count;
    }

    public String getContactName() {
        if (contactName != null && !contactName.isEmpty()) {
            return contactName;
        } else {
            return address;
        }
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Sms{");
        sb.append("address=").append(address);
        sb.append(", contactName=").append(contactName);
        sb.append(", date=").append(new Date(timeMs));
        sb.append(", count=").append(count);
        sb.append(", numberOfReceived=").append(numberOfReceived);
        sb.append(", numberOfSent=").append(numberOfSent);
        sb.append(", numberOfBlocked=").append(numberOfBlocked);
        sb.append(", type=").append(type);
        sb.append(", period=").append(period);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sms sms = (Sms) o;
        return Objects.equals(timeMs, sms.timeMs) &&
                Objects.equals(address, sms.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeMs, address);
    }

    @Override
    public int compareTo(Sms sms) {
        return this.count.compareTo(sms.count);
    }

    public static class Builder {
        @Expose(serialize = false, deserialize = false)
        private boolean isRead;
        @Expose(serialize = false, deserialize = false)
        private String status;
        @Expose(serialize = false, deserialize = false)
        private String seen;
        @Expose(serialize = false, deserialize = false)
        private Long timeMs;
        @Expose(serialize = false, deserialize = false)
        private String address;
        @Expose(serialize = false, deserialize = false)
        private String body;
        @Expose(serialize = false, deserialize = false)
        private String type;
        @Expose(serialize = false, deserialize = false)
        private String period;
        @Expose(serialize = false, deserialize = false)
        private Integer numberOfReceived = 0;
        @Expose(serialize = false, deserialize = false)
        private Integer numberOfSent = 0;
        @Expose(serialize = false, deserialize = false)
        private Integer numberOfBlocked = 0;
        @Expose(serialize = false, deserialize = false)
        private Integer count = 0;
        @Expose(serialize = false, deserialize = false)
        private String contactName;

        private Builder() {
        }

        public Builder setIsRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder setSeen(String seen) {
            this.seen = seen;
            return this;
        }

        public Builder setDate(Long timeMs) {
            this.timeMs = timeMs;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setPeriod(String period) {
            this.period = period;
            return this;
        }

        public Builder setNumberOfReceived(int numberOfReceived) {
            this.numberOfReceived = numberOfReceived;
            return this;
        }

        public Builder setNumberOfSent(int numberOfSent) {
            this.numberOfSent = numberOfSent;
            return this;
        }

        public Builder setNumberOfBlocked(int numberOfBlocked) {
            this.numberOfBlocked = numberOfBlocked;
            return this;
        }

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public Builder setContactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public Builder of(Sms sms) {
            this.isRead = sms.isRead;
            this.status = sms.status;
            this.seen = sms.seen;
            this.timeMs = sms.timeMs;
            this.address = sms.address;
            this.body = sms.body;
            this.type = sms.type;
            this.period = sms.period;
            this.numberOfReceived = sms.numberOfReceived;
            this.numberOfSent = sms.numberOfSent;
            this.numberOfBlocked = sms.numberOfBlocked;
            this.count = sms.count;
            this.contactName = sms.contactName;
            return this;
        }

        public Sms build() {
            return new Sms(this);
        }
    }
}
