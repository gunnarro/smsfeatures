package com.gunnarro.android.ughme.model.cloud;

import org.jetbrains.annotations.NotNull;

/**
 * Delombok following annotations:
 * ToString
 * Getter
 * Builder
 */
public class Dimension {
    private final int width;
    private final int height;

    Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static DimensionBuilder builder() {
        return new DimensionBuilder();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @NotNull
    public String toString() {
        return "Dimension(width=" + this.getWidth() + ", height=" + this.getHeight() + ")";
    }

    public static class DimensionBuilder {
        private int width;
        private int height;

        DimensionBuilder() {
        }

        public DimensionBuilder width(int width) {
            this.width = width;
            return this;
        }

        public DimensionBuilder height(int height) {
            this.height = height;
            return this;
        }

        public Dimension build() {
            return new Dimension(width, height);
        }
    }
}
