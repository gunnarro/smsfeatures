package com.gunnarro.android.ughme.ui.fragment.domain;

public class ListItem {
    public String id;
    public String content;
    public String details;

    public ListItem(String id) {
        this.id = id;
    }
    public ListItem(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return id + ", " + content + ", " + details;
    }
}

