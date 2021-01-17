package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Paint;
import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Word {
    private final String text;
    private final Paint paint;
    private Rect rect;
    private final int count;
    private final float size;
    private final float rotationAngle;

    private Word(Builder builder) {
        this.text = Objects.requireNonNull(builder.text);
        this.paint = Objects.requireNonNull(builder.paint);
        this.rect = Objects.requireNonNull(builder.rect);
        this.count = Objects.requireNonNull(builder.count, "count");
        this.size = Objects.requireNonNull(builder.size, "size");
        this.rotationAngle = builder.rotationAngle;
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

    public float getRotationAngle() {
        return rotationAngle;
    }

    /**
     * x pos for drawing into canvas
     */
    public float getX() {
        return rect.left;
    }

    /**
     * y pos for drawing into canvas
     */
    public float getY() {
        return rect.top;
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Word{");
        sb.append("word=").append(text);
        sb.append(", size=").append(size);
        sb.append(", count=").append(count);
        sb.append(", rect=").append(rect.toShortString()).append(" width=").append(rect.width()).append(" height=").append(rect.height());
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private String text;
        private Paint paint;
        private Rect rect;
        private Integer count;
        private Float size;
        private Float rotationAngle;

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

        public Builder setRotationAngle(float rotationAngle) {
            this.rotationAngle = rotationAngle;
            return this;
        }

        public Builder of(Word word) {
            this.text = word.text;
            this.paint = word.paint;
            this.rect = word.rect;
            this.count = word.count;
            this.size = word.size;
            this.rotationAngle = word.rotationAngle;
            return this;
        }

        public Word build() {
            return new Word(this);
        }
    }
}
