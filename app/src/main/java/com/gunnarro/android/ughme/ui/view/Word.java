package com.gunnarro.android.ughme.ui.view;

import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Objects;

public class Word {
    private final String text;
    private final Paint paint;
    private final Rect rect;
    private int count = 1;
    private float size = 0;
    private float rotationDegree = 0f;
    private float yOffset = 0f;

    private Word(Builder builder) {
        this.text = builder.text;
        this.paint = builder.paint;
        this.rect = builder.rect;
        this.count = Objects.requireNonNull(builder.count, "count");
        this.size = Objects.requireNonNull(builder.size, "size");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getText() {
        return text;
    }

    public Paint getPaint() {
        return paint;
    }

    public Rect getRect() {
        return rect;
    }

    public Integer getCount() {
        return count;
    }

    public float getSize() {
        return size;
    }

    public float getRotationDegree() {
        return rotationDegree;
    }

    public void setRotationDegree(float rotationDegree) {
        this.rotationDegree = rotationDegree;
    }

    /**
     * x pos for drawing into canvas
     *
     * @return
     */
    public float getX() {
        return (float) rect.left;
    }

    /**
     * y pos for drawing into canvas
     *
     * @return
     */
    public float getY() {
        return rect.top;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Word{");
        sb.append("text=").append(text);
        sb.append(", count=").append(count);
        sb.append(", rect=").append(rect.toShortString());
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private String text;
        private Paint paint;
        private Rect rect;
        private Integer count;
        private Float size;
        private Float yOffset;

        private Builder() {
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setPaint(Paint paint) {
            this.paint = paint;
            return this;
        }

        public Builder setRect(Rect rect) {
            this.rect = rect;
            return this;
        }

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public Builder setSize(float size) {
            this.size = size;
            return this;
        }

        public Builder setYOffset(float yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Builder of(Word word) {
            this.text = word.text;
            this.paint = word.paint;
            this.rect = word.rect;
            this.count = word.count;
            this.size = word.size;
            this.yOffset = word.yOffset;
            return this;
        }

        public Word build() {
            return new Word(this);
        }
    }
}
