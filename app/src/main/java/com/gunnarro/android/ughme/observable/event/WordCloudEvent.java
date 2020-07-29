package com.gunnarro.android.ughme.observable.event;

import java.util.Objects;

public class WordCloudEvent {
    public enum WordCloudEventTypeEnum {
        MESSAGE, NUMBER, DATE
    }

    private static final String MESSAGE_TYPE_ALL = "(.*)";
    private static final String MESSAGE_TYPE_INBOX = "1";
    private static final String MESSAGE_TYPE_OUTBOX = "2";

    private final WordCloudEventTypeEnum eventType;
    private final String smsType;
    private final String value;

    private WordCloudEvent(Builder builder) {
        this.eventType = Objects.requireNonNull(builder.eventType, "eventType");
        this.smsType = Objects.requireNonNull(builder.smsType, "smsType");
        this.value = builder.value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public WordCloudEvent.WordCloudEventTypeEnum getEventType() {
        return eventType;
    }

    public String getSmsType() {
        return smsType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WordCloudEvent{");
        sb.append("eventType=").append(eventType);
        sb.append(", smsType=").append(smsType);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder class
     */
    public static class Builder {
        private WordCloudEventTypeEnum eventType;
        private String smsType;
        private String value;

        private Builder() {
        }

        public Builder setEventType(WordCloudEventTypeEnum eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder smsTypeAll() {
            this.smsType = MESSAGE_TYPE_ALL;
            return this;
        }

        public Builder smsTypeInbox() {
            this.smsType = MESSAGE_TYPE_INBOX;
            return this;
        }

        public Builder smsTypeOutbox() {
            this.smsType = MESSAGE_TYPE_OUTBOX;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder of(WordCloudEvent wordCloudEvent) {
            this.eventType = wordCloudEvent.eventType;
            this.smsType = wordCloudEvent.smsType;
            this.value = wordCloudEvent.value;
            return this;
        }

        public WordCloudEvent build() {
            return new WordCloudEvent(this);
        }
    }
}
