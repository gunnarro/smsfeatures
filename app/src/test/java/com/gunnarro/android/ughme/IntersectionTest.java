package com.gunnarro.android.ughme;

import android.util.Log;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class IntersectionTest {

    @Test
    public void intersection() {
        /**
        int left = 0;// x0
        int top = 10; // y0
        int right = 6; // x1
        int bottom = 0; // y1
        */
        //l1.x=0;l1.y=10; r1.x=10;r1.y=0;
        //l2.x=5;l2.y=5; r2.x=15;r2.y=0;

        Rect a = new Rect(5, 5, 7, 0);
        Rect b = new Rect(0, 8, 6, 0);
        Log.d("unit-test", String.format("a=%s", a));
        Log.d("unit-test", String.format("b=%s", b));

       // Assert.assertEquals(null, intersects(a,b));
        Rect abunion = checkIntersect(a,b);
       // Assert.assertEquals("", abunion);
        Log.d("unit-test", String.format("a-b union=%s", abunion));
        // always move b if overlap
        // try first move down, the right, up, and left
        b.offset(0, Math.abs(abunion.height()));
        Log.d("unit-test", String.format("moved down, b=%s", b));
    //    Assert.assertEquals(null, intersects(a,b));
        /**
        // move right
        b.offset(Math.abs(abunion.width()), 0);
        Assert.assertEquals(null, checkIntersect(a,b));
        // move up
        b.offset(0, Math.abs(abunion.height())*-1);
        Assert.assertEquals(null, checkIntersect(a,b));
        // move left
        b.offset(Math.abs(abunion.width())*-1, 0);
        Assert.assertEquals(null, checkIntersect(a,b));
*/
        /**
         Assert.assertEquals(true, checkOverlapLeft(a,b));
         Assert.assertEquals(true, checkOverlapAbove(a,b));
         Assert.assertEquals("", union(a,b));
 */

    }

    public boolean checkOverlap(Rect a, Rect b) {
        return checkOverlapAbove(a,b) || checkOverlapLeft(a,b);
    }
    /**
     * Returns true if two rectangles a and b overlap
     */
    static boolean checkOverlapLeft(Rect a, Rect b) {
        // If one rectangle is on left side of other
        if (a.left >= b.right ) return false;
        if (b.left >= a.right) return false;
        Log.d("unit-test", String.format("overlap left, %s => %s || %s => %s", a.left, b.right, b.left, a.right));
        return true;
    }

    static boolean checkOverlapAbove(Rect a, Rect b) {
        // If one rectangle is above other
        if (a.top <= b.bottom) return false;
        if (b.top <= a.bottom) return false;
        Log.d("unit-test", String.format("overlap above, %s <= %s || %s <= %s", a.top, b.bottom, b.top, a.bottom));
        return true;
    }

    /**
     * left:   The X coordinate of the left side of the rectangle
     * top:    The Y coordinate of the top of the rectangle
     * right:  The X coordinate of the right side of the rectangle
     * bottom: The Y coordinate of the bottom of the rectangle
     *
     */
    public static boolean intersects(Rect a, Rect b) {
        Log.d("unit-test", String.format("intersects, %s < %s && %s < %s", a.left, b.right, b.left, a.right));
        Log.d("unit-test", String.format("intersects, %s < %s && %s < %s", a.top, b.bottom, b.top, a.bottom));
        return a.left < b.right && b.left < a.right
                && a.top < b.bottom && b.top < a.bottom;
    }

    public Rect checkIntersect(Rect a, Rect b) {
        Log.d("unit-test", String.format("checkIntersect, a=%s", a));
        Log.d("unit-test", String.format("checkIntersect, a=%s", b));
        if (checkOverlap(a, b)) {
            int left = Math.max(a.left, b.left);
            int top = Math.max(a.top, b.top);
            int right = Math.min(a.right, b.right);
            int bottom = Math.min(a.bottom, b.bottom);
            return new Rect(left, top, right, bottom);
        }
        return null;
    }


}
