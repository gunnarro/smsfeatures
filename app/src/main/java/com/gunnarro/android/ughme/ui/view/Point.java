package com.gunnarro.android.ughme.ui.view;

import java.util.Objects;

public class Point {

    private final int x;
    private final int y;

    private Point(Builder builder) {
        this.x = Objects.requireNonNull(builder.x, "x");
        this.y = Objects.requireNonNull(builder.y, "y");
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static class Builder {
        private Integer x;
        private Integer y;

        private Builder() {
        }

        public Builder setX(int x) {
            this.x = x;
            return this;
        }

        public Builder setY(int y) {
            this.y = y;
            return this;
        }

        public Builder of(Point point) {
            this.x = point.x;
            this.y = point.y;
            return this;
        }

        public Point build() {
            return new Point(this);
        }
    }
}
