package com.gunnarro.android.ughme.observable.event;

import com.gunnarro.android.ughme.model.cloud.Word;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
public class WordCloudEvent {
    public static final String MESSAGE_TYPE_ALL = "(.*)";
    public static final String MESSAGE_TYPE_INBOX = "1";
    public static final String MESSAGE_TYPE_OUTBOX = "2";
    private final WordCloudEventTypeEnum eventType;
    private final String smsType;
    private final String value;
    private final List<Word> wordList;

    private WordCloudEvent(Builder builder) {
        this.eventType = Objects.requireNonNull(builder.eventType, "eventType");
        this.smsType = Objects.requireNonNull(builder.smsType, "smsType");
        this.value = builder.value;
        this.wordList = Objects.requireNonNull(builder.wordList, "wordList");
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isUpdateEvent() {
        return eventType.equals(WordCloudEventTypeEnum.UPDATE_MESSAGE);
    }

    public String getValue() {
        return value;
    }

    public List<Word> getWordList() {
        return wordList;
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WordCloudEvent{");
        sb.append("eventType=").append(eventType);
        sb.append(", smsType=").append(smsType);
        sb.append(", value=").append(value);
        sb.append(", wordListSize=").append(wordList.size());
        sb.append('}');
        return sb.toString();
    }

    public enum WordCloudEventTypeEnum {
        MESSAGE, UPDATE_MESSAGE
    }

    /**
     * Builder class
     */
    public static class Builder {
        private WordCloudEventTypeEnum eventType;
        private String smsType;
        private String value;
        private List<Word> wordList;

        private Builder() {
        }

        public Builder eventType(WordCloudEventTypeEnum eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder smsTypeAll() {
            this.smsType = MESSAGE_TYPE_ALL;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder wordList(List<Word> wordList) {
            this.wordList = wordList;
            return this;
        }

        public Builder of(WordCloudEvent wordCloudEvent) {
            this.eventType = wordCloudEvent.eventType;
            this.smsType = wordCloudEvent.smsType;
            this.value = wordCloudEvent.value;
            this.wordList = wordCloudEvent.wordList;
            return this;
        }

        public WordCloudEvent build() {
            return new WordCloudEvent(this);
        }
    }
}
