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
    private final List<Word> wordList;
    private final String progressMsg;
    private final int progressStep;
    private final int animationInterval;

    private WordCloudEvent(Builder builder) {
        this.eventType = Objects.requireNonNull(builder.eventType, "eventType");
        this.wordList = Objects.requireNonNull(builder.wordList, "wordList");
        this.animationInterval = builder.animationInterval;
        this.progressMsg = builder.progressMsg;
        this.progressStep = builder.progressStep;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isUpdateEvent() {
        return eventType.equals(WordCloudEventTypeEnum.UPDATE_MESSAGE);
    }

    public boolean isProgressEvent() {
        return eventType.equals(WordCloudEventTypeEnum.PROGRESS);
    }

    public List<Word> getWordList() {
        return wordList;
    }

    public String getProgressMsg() {
        return progressMsg;
    }

    public int getProgressStep() {
        return progressStep;
    }

    public int getAnimationInterval() { return animationInterval; }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WordCloudEvent{");
        sb.append("eventType=").append(eventType);
        sb.append(", wordListSize=").append(wordList.size());
        sb.append('}');
        return sb.toString();
    }

    public enum WordCloudEventTypeEnum {
        UPDATE_MESSAGE, PROGRESS
    }

    /**
     * Builder class
     */
    public static class Builder {
        private WordCloudEventTypeEnum eventType;
        private List<Word> wordList;
        private int animationInterval;
        private String progressMsg;
        private int progressStep;

        private Builder() {
        }

        public Builder eventType(WordCloudEventTypeEnum eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder wordList(List<Word> wordList) {
            this.wordList = wordList;
            return this;
        }

        public Builder animationInterval(int animationInterval) {
            this.animationInterval = animationInterval;
            return this;
        }

        public Builder progressMsg(String progressMsg) {
            this.progressMsg = progressMsg;
            return this;
        }

        public Builder progressStep(int progressStep) {
            this.progressStep = progressStep;
            return this;
        }

        public Builder of(WordCloudEvent wordCloudEvent) {
            this.eventType = wordCloudEvent.eventType;
            this.wordList = wordCloudEvent.wordList;
            this.animationInterval = wordCloudEvent.animationInterval;
            this.progressMsg = wordCloudEvent.progressMsg;
            this.progressStep = wordCloudEvent.progressStep;
            return this;
        }

        public WordCloudEvent build() {
            return new WordCloudEvent(this);
        }
    }
}
