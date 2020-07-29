package com.gunnarro.android.ughme;

public class Rect {

    int left;
    int top;
    int right;
    int bottom;

    public final int width() {
        return right - left;
    }

    public final int height() {
        return bottom - top;
    }

    public void offset(int dx, int dy) {
        left += dx;
        top += dy;
        right += dx;
        bottom += dy;
    }

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Rect{");
        sb.append("left=").append(left);
        sb.append(", top=").append(top);
        sb.append(", right=").append(right);
        sb.append(", bottom=").append(bottom);
        sb.append(", width=").append(width());
        sb.append(", height=").append(height());
        sb.append('}');
        return sb.toString();
    }
}
