package com.gunnarro.android.ughme.modle.cloud;

import com.gunnarro.android.ughme.model.cloud.TreeWordPlacer;

import org.junit.Test;

public class TreeWordPlacerTest {

    // entry=Entry [value=det, geometry=Rectangle   [x1=448.0, y1=903.0, x2=993.0, y2=1210.0]]
    // entry=Entry [value=sveen, geometry=Rectangle [x1=430.0, y1=789.0, x2=929.0, y2=899.0]]
    @Test
    public void checkRectangle() {
        TreeWordPlacer treeWordPlacer = new TreeWordPlacer();
        treeWordPlacer.place("det", null);
        treeWordPlacer.place("det", null);
    }
}