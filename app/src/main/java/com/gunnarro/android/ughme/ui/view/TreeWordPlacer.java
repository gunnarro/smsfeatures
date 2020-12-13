package com.gunnarro.android.ughme.ui.view;

import android.graphics.Rect;
import android.util.Log;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;

import rx.Observable;

public class TreeWordPlacer {

    private final static String TAG = TreeWordPlacer.class.getSimpleName();

    private RTree<String, Rectangle> placedWordRTree;

    public void reset() {
        placedWordRTree = RTree.maxChildren(4).create();
    }

    public boolean place(final String word, Rect wordRect) {
       // Log.d(TAG, String.format("word=%s, rect=%s", word, wordRect.toShortString()));
        final Rectangle wordRectangle = Geometries.rectangle(
                (float)wordRect.left,
                (float)wordRect.top,
                (float)(wordRect.left + wordRect.width()),
                (float)(wordRect.top + wordRect.height()));

        final Observable<Entry<String, Rectangle>> results = placedWordRTree.search(wordRectangle);
        final int matches = results.count().toBlocking().single();
        if (matches > 0) {
         //   Log.d(TAG, String.format("collision, word=%s, position=%s,%s, matches=%s, tree-size=%s", word, wordRect.left, wordRect.top, matches, placedWordRTree.size()));
            return false;
        }
        placedWordRTree = placedWordRTree.add(word, wordRectangle);
        return true;
    }
}
