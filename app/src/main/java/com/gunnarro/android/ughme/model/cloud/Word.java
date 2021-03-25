package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Paint;
import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

/**
 * Delombok follwoing annotations:
 * ToString
 * Getter
 * Builder
 */
public class Word {
    String text;
    Paint paint;
    Rect rect;
    Integer count;
    float size;
    float rotationAngle;

    Word(String text, Paint paint, Rect rect, Integer count, float size, float rotationAngle) {
        this.text = text;
        this.paint = paint;
        this.rect = rect;
        this.count = count;
        this.size = size;
        this.rotationAngle = rotationAngle;
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

    public float getSize() {
        return this.size;
    }

    public float getRotationAngle() {
        return this.rotationAngle;
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
        private float size;
        private float rotationAngle;

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

        public WordBuilder size(float size) {
            this.size = size;
            return this;
        }

        public WordBuilder rotationAngle(float rotationAngle) {
            this.rotationAngle = rotationAngle;
            return this;
        }

        public Word build() {
            return new Word(text, paint, rect, count, size, rotationAngle);
        }

    }
}
