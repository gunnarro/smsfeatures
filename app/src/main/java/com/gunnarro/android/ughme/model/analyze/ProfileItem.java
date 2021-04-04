package com.gunnarro.android.ughme.model.analyze;

public class ProfileItem {
    private final String className;
    private final String method;
    private final long executionTime;

    public ProfileItem(String className, String method, long executionTime) {
        this.className = className;
        this.method = method;
        this.executionTime = executionTime;
    }

    public static ProfileItem.ProfileItemBuilder builder() {
        return new ProfileItem.ProfileItemBuilder();
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

    public static class ProfileItemBuilder {
        private String className;
        private String method;
        private long executionTime;

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
        public ProfileItem build() {
            return new ProfileItem(className, method, executionTime);
        }

    }
}
