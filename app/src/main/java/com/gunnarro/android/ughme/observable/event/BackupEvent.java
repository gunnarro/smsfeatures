package com.gunnarro.android.ughme.observable.event;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
public class BackupEvent {
    private final BackupEventEventTypeEnum eventType;

    private BackupEvent(Builder builder) {
        this.eventType = Objects.requireNonNull(builder.eventType, "eventType");
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isBackupFinished() {
        return eventType.equals(BackupEventEventTypeEnum.BACKUP_FINISHED);
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BackupEvent{");
        sb.append("eventType=").append(eventType);
        sb.append('}');
        return sb.toString();
    }

    public enum BackupEventEventTypeEnum {
        BACKUP_FINISHED
    }

    /**
     * Builder class
     */
    public static class Builder {
        private BackupEventEventTypeEnum eventType;

        private Builder() {
        }

        public Builder eventType(BackupEventEventTypeEnum eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder of(BackupEvent wordCloudEvent) {
            this.eventType = wordCloudEvent.eventType;
            return this;
        }

        public BackupEvent build() {
            return new BackupEvent(this);
        }
    }
}
