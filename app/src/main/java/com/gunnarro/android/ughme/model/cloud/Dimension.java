package com.gunnarro.android.ughme.model.cloud;

public class Dimension {
    private final int width;
    private final int height;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Dimension{");
        sb.append("width=").append(width);
        sb.append(", height=").append(height);
        sb.append('}');
        return sb.toString();
    }
}
