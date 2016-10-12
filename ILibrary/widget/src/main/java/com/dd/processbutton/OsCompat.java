package com.dd.processbutton;

import android.content.Context;
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

    public static int getResourceColorCompat(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(resId, null);
        }
        return context.getResources().getColor(resId);
    }

    public static Drawable getResourceDrawableCompat(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(resId, null);
        }
        return context.getResources().getDrawable(resId);
    }
}
