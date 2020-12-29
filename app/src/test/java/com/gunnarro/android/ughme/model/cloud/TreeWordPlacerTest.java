package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Paint;
import android.graphics.Rect;

import com.gunnarro.android.ughme.model.cloud.TreeWordPlacer;
import com.gunnarro.android.ughme.model.cloud.Word;

import org.junit.Assert;
import org.junit.Test;

public class TreeWordPlacerTest {

    @Test
    public void place() {
        TreeWordPlacer treeWordPlacer = new TreeWordPlacer();
        treeWordPlacer.reset();

        android.graphics.Rect wordRect = new Rect();
        wordRect.offsetTo(775, 775);
        Word word = Word.builder()
                .setText("test")
                .setRect(wordRect)
                .setPaint(new Paint())
                .setCount(1)
                .setSize(10)
                .build();

        Assert.assertTrue(treeWordPlacer.place(word.getText(), word.getRect()));
        Assert.assertFalse(treeWordPlacer.place(word.getText(), word.getRect()));
    }
}
