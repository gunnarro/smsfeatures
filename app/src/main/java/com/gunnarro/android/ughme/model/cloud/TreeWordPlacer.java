package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Rect;
import android.util.Log;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.gunnarro.android.ughme.exception.ApplicationException;

import rx.Observable;

public class TreeWordPlacer {

    private RTree<String, Rectangle> placedWordRTree;

    public void reset() {
        placedWordRTree = RTree.maxChildren(4).create();
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

        //checkRect(wordRect, wordRectangle);

        final Observable<Entry<String, Rectangle>> results = placedWordRTree.search(wordRectangle);
        final int matches = results.count().toBlocking().single();
        if (matches > 0) {
            return false;
        }
        placedWordRTree = placedWordRTree.add(word, wordRectangle);
        return true;
    }

    /**
     * only for debug
     */
    private void checkRect(Rect wordRect, Rectangle wordRectangle) {
        if (wordRect.left + wordRect.width() != wordRect.right) {
            throw new ApplicationException("TreeWordPlacer: X CONVERTED NOT EQUAL", null);
        }

        if (wordRect.top + wordRect.height() != wordRect.bottom) {
            throw new ApplicationException("TreeWordPlacer: Y CONVERTED NOT EQUAL", null);
        }

        if (wordRect.left != wordRectangle.x1() || wordRect.right != wordRectangle.x2()) {
            throw new ApplicationException("TreeWordPlacer: X MAPPED NOT EQUAL", null);
        }

        if (wordRect.top != wordRectangle.y1() || wordRect.bottom != wordRectangle.y2()) {
            throw new ApplicationException("TreeWordPlacer: Y MAPPED NOT EQUAL", null);
        }
    }
}
