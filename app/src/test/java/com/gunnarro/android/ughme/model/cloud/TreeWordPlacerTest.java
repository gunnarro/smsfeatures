package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Paint;
import android.graphics.Rect;

import com.gunnarro.android.ughme.service.impl.TreeWordPlacer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TreeWordPlacerTest {

    private Rect wordRect;

    @Before
    public void init() {
        wordRect = Mockito.mock(Rect.class);
        Mockito.doNothing().when(wordRect).offsetTo(Mockito.isA(Integer.class), Mockito.isA(Integer.class));
    }

    @Test
    public void place() {
        TreeWordPlacer treeWordPlacer = new TreeWordPlacer();
        treeWordPlacer.reset();
        wordRect.offsetTo(775, 775);
        Word word = Word.builder()
                .text("test")
                .rect(wordRect)
                .paint(new Paint())
                .count(1)
                .size(10)
                .rotationAngle(0)
                .build();

        Assert.assertTrue(treeWordPlacer.place(word.getText(), word.getRect()));
        Assert.assertFalse(treeWordPlacer.place(word.getText(), word.getRect()));
    }
}
