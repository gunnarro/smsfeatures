package com.gunnarro.android.ughme.model.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileItem {
    private String className;
    private String method;
    private long executionTime;
    private String exception;

    /**
     * Used by jackson
     */
    ProfileItem() {
    }

    public ProfileItem(String className, String method, long executionTime, String exception) {
        this.className = className;
        this.method = method;
        this.executionTime = executionTime;
        this.exception = exception;
    }

    public String getClassName() {
        return className;
    }

    public String getMethod() {
        return method;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getException() {
        return exception;
    }

    public static ProfileItem.ProfileItemBuilder builder() {
        return new ProfileItem.ProfileItemBuilder();
    }

    public static class ProfileItemBuilder {
        private String className;
        private String method;
        private long executionTime;
        private String exception;

        ProfileItemBuilder() {
        }

        public ProfileItem.ProfileItemBuilder className(String className) {
            this.className = className;
            return this;
        }

        public ProfileItem.ProfileItemBuilder method(String method) {
            this.method = method;
            return this;
        }

        public ProfileItem.ProfileItemBuilder executionTime(long executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public ProfileItem.ProfileItemBuilder exception(String exception) {
            this.exception = exception;
            return this;
        }

        public ProfileItem build() {
            return new ProfileItem(className, method, executionTime, exception);
        }

    }
}
