package com.gunnarro.android.ughme.ui.fragment.domain;

import org.jetbrains.annotations.NotNull;

public class ListItem {
    private String id;
    private String content;
    private String details;

    public ListItem(String id) {
        this.id = id;
    }

    public ListItem(final String id, final String content, final String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getDetails() {
        return details;
    }

    @Override
    @NotNull
    public String toString() {
        return id + ", " + content + ", " + details;
    }
}

