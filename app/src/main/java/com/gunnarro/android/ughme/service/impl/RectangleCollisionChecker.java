package com.gunnarro.android.ughme.service.impl;

import android.graphics.Rect;

public class RectangleCollisionChecker {

    public boolean intersects(final Rect rect1, final Rect rect2) {
        if ((rect1.left + rect1.width() < rect2.left)
                || (rect2.left + rect2.width() < rect1.left)) {
            return false;
        }
        return (rect1.top + rect1.height() >= rect2.top)
                && (rect2.top + rect2.height() >= rect1.top);
    }
}
