package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Paint;
import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

/**
 * Holds all information in order to place and draw a word into the word cloud
 * <p>
 * Delombok following annotations:
 * ToString
 * Getter
 * Builder
 */
public class Word {
    enum StatusEnum {
        PLACED, NOT_PLACED
    }

    final String text;
    StatusEnum status;
    final Paint paint;
    final Rect rect;
    final Integer count;
    final Integer category;
    final float size;
    float rotationAngle;
    boolean rotate;

    Word(String text, Paint paint, Rect rect, Integer count, Integer category, float size, float rotationAngle, boolean rotate) {
        this.text = text;
        this.paint = paint;
        this.rect = rect;
        this.count = count;
        this.category = category;
        this.size = size;
        this.rotationAngle = rotationAngle;
        this.rotate = rotate;
    }

    public static WordBuilder builder() {
        return new WordBuilder();
    }

    public int getX() {
        return rect.left;
    }

    public int getY() {
        return rect.top;
    }

    public String getText() {
        return this.text;
    }

    public Paint getPaint() {
        return this.paint;
    }

    public Rect getRect() {
        return this.rect;
    }

    public Integer getCount() {
        return this.count;
    }

    public Integer getCategory() {
        return this.category;
    }

    public float getSize() {
        return this.size;
    }

    public float getRotationAngle() {
        return this.rotationAngle;
    }

    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void setStatusPlaced() {
        status = StatusEnum.PLACED;
    }

    public void setStatusNotPlaced() {
        status = StatusEnum.NOT_PLACED;
    }

    public boolean isPlaced() {
        return status.equals(StatusEnum.PLACED);
    }

    public boolean isNotPlaced() {
        return status.equals(StatusEnum.NOT_PLACED);
    }

    public void setRotate(boolean isRotate) {
        rotate = isRotate;
    }

    public boolean isRotate() {
        return rotate;
    }

    @NotNull
    public String toString() {
        return "Word(text=" + this.getText() + ", rect=" + this.getRect() + ", count=" + this.getCount() + ", size=" + this.getSize() + ", rotationAngle=" + this.getRotationAngle() + ")";
    }

    public static class WordBuilder {
        private String text;
        private Paint paint;
        private Rect rect;
        private Integer count;
        private Integer category;
        private float size;
        private float rotationAngle;
        private boolean rotate;

        WordBuilder() {
        }

        public WordBuilder text(String text) {
            this.text = text;
            return this;
        }

        public WordBuilder paint(Paint paint) {
            this.paint = paint;
            return this;
        }

        public WordBuilder rect(Rect rect) {
            this.rect = rect;
            return this;
        }

        public WordBuilder count(Integer count) {
            this.count = count;
            return this;
        }

        public WordBuilder category(Integer category) {
            this.category = category;
            return this;
        }

        public WordBuilder size(float size) {
            this.size = size;
            return this;
        }

        public WordBuilder rotationAngle(float rotationAngle) {
            this.rotationAngle = rotationAngle;
            return this;
        }

        public WordBuilder rotate(boolean rotate) {
            this.rotate = rotate;
            return this;
        }

        public Word build() {
            return new Word(text, paint, rect, count, category, size, rotationAngle, rotate);
        }

    }
}
