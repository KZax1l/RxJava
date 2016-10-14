package com.dd.processbutton;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

public class OsCompat {
    public static void setBackgroundDrawableCompat(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static int getResourceColorCompat(View view, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return view.getResources().getColor(resId, null);
        }
        return view.getResources().getColor(resId);
    }

    public static Drawable getResourceDrawableCompat(View view, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return view.getResources().getDrawable(resId, null);
        }
        return view.getResources().getDrawable(resId);
    }
}
