package com.gunnarro.android.ughme.service.impl;

import android.graphics.Rect;
import android.util.Log;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class TreeWordPlacer {

    private RTree<String, Rectangle> placedWordRTree;
    private Map<String, Integer> attemptsMap;

    public void reset() {
        placedWordRTree = RTree.maxChildren(RTree.MAX_CHILDREN_DEFAULT_GUTTMAN).create();
        attemptsMap = new HashMap<>();
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
                (float) wordRect.left == 0 ? 10 : 10,
                (float) wordRect.top == 0 ? 10 : 10,
                (float) wordRect.right == 0 ? 25 : 25,
                (float) wordRect.bottom == 0 ? 25 : 25);
        final Observable<Entry<String, Rectangle>> results = placedWordRTree.search(wordRectangle);
        final int matches = results.count().toBlocking().single();
        updateAttempts(word);
        if (matches > 0) {
            return false;
        }
        placedWordRTree = placedWordRTree.add(word, wordRectangle);
        return true;
    }

    private void updateAttempts(String word) {
        if (attemptsMap.containsKey(word)) {
            attemptsMap.put(word, attemptsMap.get(word) + 1);
        } else {
            attemptsMap.put(word, 1);
        }
    }

    public void printAttempts() {
        attemptsMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach( e ->
                Log.i("TreeWordPlacer.place", String.format("word=%s, attempts=%s", e.getKey(), e.getValue())));
    }

    public void saveTreeAsImage() {
        Log.d("wordcloudplacer", placedWordRTree.asString());
        placedWordRTree.visualize(1000,1000).save("src/test/resources/wordcloudTree.png", "png");
    }
}

