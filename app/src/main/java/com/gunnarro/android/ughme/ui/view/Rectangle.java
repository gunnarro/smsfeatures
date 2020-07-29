package com.gunnarro.android.ughme.ui.view;


public class Rectangle {
    private final Point bottomLeft;
    private final Point topRight;

    private Rectangle(Builder builder) {
        this.bottomLeft = builder.bottomLeft;
        this.topRight = builder.topRight;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isOverlapping(Rectangle other) {
        if (this.topRight.getY() < other.bottomLeft.getY()
                || this.bottomLeft.getY() > other.topRight.getY()) {
            return false;
        }
        if (this.topRight.getX() < other.bottomLeft.getX()
                || this.bottomLeft.getX() > other.topRight.getX()) {
            return false;
        }
        return true;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public Point getTopRight() {
        return topRight;
    }

    public static class Builder {
        private Point bottomLeft;
        private Point topRight;

        private Builder() {
        }

        public Builder setBottomLeft(Point bottomLeft) {
            this.bottomLeft = bottomLeft;
            return this;
        }

        public Builder setTopRight(Point topRight) {
            this.topRight = topRight;
            return this;
        }

        public Builder of(Rectangle rectangle) {
            this.bottomLeft = rectangle.bottomLeft;
            this.topRight = rectangle.topRight;
            return this;
        }

        public Rectangle build() {
            return new Rectangle(this);
        }
    }
}
