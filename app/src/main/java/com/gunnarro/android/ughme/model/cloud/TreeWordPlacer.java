package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Rect;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;

import rx.Observable;

public class TreeWordPlacer {

    private RTree<String, Rectangle> placedWordRTree;

    public void reset() {
        placedWordRTree = RTree.maxChildren(4).create();
    }

    public boolean place(final String word, Rect wordRect) {
        final Rectangle wordRectangle = Geometries.rectangle(
                (float) wordRect.left,
                (float) wordRect.top,
                (float) (wordRect.left + wordRect.width()),
                (float) (wordRect.top + wordRect.height()));

        final Observable<Entry<String, Rectangle>> results = placedWordRTree.search(wordRectangle);
        final int matches = results.count().toBlocking().single();
        if (matches > 0) {
            return false;
        }
        placedWordRTree = placedWordRTree.add(word, wordRectangle);
        return true;
    }
}
