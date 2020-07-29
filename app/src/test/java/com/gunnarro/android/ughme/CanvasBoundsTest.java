package com.gunnarro.android.ughme;

import android.graphics.Point;

import com.gunnarro.android.ughme.ui.view.WordCloudBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CanvasBoundsTest {

    @Mock
    Point pointMock;

    @Test
    public void isInsideCanvasBounds() {
        Mockito.when(pointMock).thenReturn(new Point(-1,0));
        WordCloudBuilder wb = new WordCloudBuilder(1000, 1000);
       // Assert.assertTrue(wb.isInsideCanvasBounds(new Point(0,0)));
        Assert.assertFalse(wb.isInsideCanvasBounds(new Point(-1,0)));
        Assert.assertFalse(wb.isInsideCanvasBounds(new Point( 0, -1)));
    }
}
