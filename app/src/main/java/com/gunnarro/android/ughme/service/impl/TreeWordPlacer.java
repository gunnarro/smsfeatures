package com.gunnarro.android.ughme.service.impl;

import android.graphics.Rect;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;

import rx.Observable;

public class TreeWordPlacer {

    private RTree<String, Rectangle> placedWordRTree;

    public void reset() {
        placedWordRTree = RTree.maxChildren(RTree.MAX_CHILDREN_DEFAULT_GUTTMAN).create();
    }

    /**
     * Rect:
     * left: The X coordinate of the left side of the rectangle
     * top: The Y coordinate of the top of the rectangle
     * right: The X coordinate of the right side of the rectangle
     * bottom: The Y coordinate of the bottom of the rectangle
     */
    public boolean place(final String word, Rect wordRect) {
        final Rectangle wordRectangle = Geometries.rectangle(
                (float) wordRect.left,
                (float) wordRect.top,
                (float) (wordRect.right),
                (float) (wordRect.bottom));
        final Observable<Entry<String, Rectangle>> results = placedWordRTree.search(wordRectangle);
        final int matches = results.count().toBlocking().single();
        if (matches > 0) {
            return false;
        }
        placedWordRTree = placedWordRTree.add(word, wordRectangle);
        return true;
    }
}
