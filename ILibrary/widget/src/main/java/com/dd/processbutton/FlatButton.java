package com.dd.processbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.Button;

import org.zsago.widget.R;

import static com.dd.processbutton.OsCompat.getResourceColorCompat;
import static com.dd.processbutton.OsCompat.getResourceDrawableCompat;
import static com.dd.processbutton.OsCompat.setBackgroundDrawableCompat;

public class FlatButton extends Button {
    private StateListDrawable mNormalDrawable;
    private CharSequence mNormalText;
    private float cornerRadius;

    public FlatButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public FlatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FlatButton(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        mNormalDrawable = new StateListDrawable();
        if (attrs != null) {
            initAttributes(context, attrs);
        }
        mNormalText = getText().toString();
        setBackgroundCompat(mNormalDrawable);
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.FlatButton);
        if (attr == null) {
            return;
        }
        try {
            float defValue = getDimension(R.dimen.process_button_corner_radius);
            cornerRadius = attr.getDimension(R.styleable.FlatButton_pb_cornerRadius, defValue);
            mNormalDrawable.addState(new int[]{android.R.attr.state_pressed}, createPressedDrawable(attr));
            mNormalDrawable.addState(new int[]{android.R.attr.state_focused}, createPressedDrawable(attr));
            mNormalDrawable.addState(new int[]{android.R.attr.state_selected}, createPressedDrawable(attr));
            mNormalDrawable.addState(new int[]{}, createNormalDrawable(attr));
        } finally {
            attr.recycle();
        }
    }

    private LayerDrawable createNormalDrawable(TypedArray attr) {
        LayerDrawable drawableNormal = (LayerDrawable) getDrawable(R.drawable.process_button_rect_normal).mutate();
        GradientDrawable drawableTop = (GradientDrawable) drawableNormal.getDrawable(0).mutate();
        drawableTop.setCornerRadius(getCornerRadius());

        int blueDark = getColor(R.color.process_button_blue_pressed);
        int colorPressed = attr.getColor(R.styleable.FlatButton_pb_colorPressed, blueDark);
        drawableTop.setColor(colorPressed);

        GradientDrawable drawableBottom = (GradientDrawable) drawableNormal.getDrawable(1).mutate();
        drawableBottom.setCornerRadius(getCornerRadius());

        int blueNormal = getColor(R.color.process_button_blue_normal);
        int colorNormal = attr.getColor(R.styleable.FlatButton_pb_colorNormal, blueNormal);
        drawableBottom.setColor(colorNormal);
        return drawableNormal;
    }

    private Drawable createPressedDrawable(TypedArray attr) {
        GradientDrawable drawablePressed = (GradientDrawable) getDrawable(R.drawable.process_button_rect_pressed).mutate();
        drawablePressed.setCornerRadius(getCornerRadius());

        int blueDark = getColor(R.color.process_button_blue_pressed);
        int colorPressed = attr.getColor(R.styleable.FlatButton_pb_colorPressed, blueDark);
        drawablePressed.setColor(colorPressed);

        return drawablePressed;
    }

    protected Drawable getDrawable(int id) {
        return getResourceDrawableCompat(this, id);
    }

    protected float getDimension(int id) {
        return getResources().getDimension(id);
    }

    protected int getColor(int id) {
        return getResourceColorCompat(this, id);
    }

    protected TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public StateListDrawable getNormalDrawable() {
        return mNormalDrawable;
    }

    public CharSequence getNormalText() {
        return mNormalText;
    }

    public void setNormalText(CharSequence normalText) {
        mNormalText = normalText;
    }

    /**
     * Set the View's background. Masks the API changes made in Jelly Bean.
     */
    public void setBackgroundCompat(Drawable drawable) {
        int pL = getPaddingLeft();
        int pT = getPaddingTop();
        int pR = getPaddingRight();
        int pB = getPaddingBottom();

        setBackgroundDrawableCompat(this, drawable);
        setPadding(pL, pT, pR, pB);
    }
}
