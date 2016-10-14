package com.dd;

import android.graphics.drawable.GradientDrawable;

class StrokeGradientDrawable {

    private int mStrokeWidth;
    private int mStrokeColor;

    private GradientDrawable mGradientDrawable;

    StrokeGradientDrawable(GradientDrawable drawable) {
        mGradientDrawable = drawable;
    }

    private int getStrokeWidth() {
        return mStrokeWidth;
    }

    void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
        mGradientDrawable.setStroke(strokeWidth, getStrokeColor());
    }

    private int getStrokeColor() {
        return mStrokeColor;
    }

    void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        mGradientDrawable.setStroke(getStrokeWidth(), strokeColor);
    }

    GradientDrawable getGradientDrawable() {
        return mGradientDrawable;
    }
}
