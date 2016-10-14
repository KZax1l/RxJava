package com.dd;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
class MorphingAnimation {

    static final int DURATION_NORMAL = 400;
    static final int DURATION_INSTANT = 1;

    private OnAnimationEndListener mListener;

    private int mDuration;

    private int mFromWidth;
    private int mToWidth;

    private int mFromColor;
    private int mToColor;

    private int mFromStrokeColor;
    private int mToStrokeColor;

    private float mFromCornerRadius;
    private float mToCornerRadius;

    private float mPadding;

    private TextView mView;
    private StrokeGradientDrawable mDrawable;

    MorphingAnimation(TextView viewGroup, StrokeGradientDrawable drawable) {
        mView = viewGroup;
        mDrawable = drawable;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    void setListener(OnAnimationEndListener listener) {
        mListener = listener;
    }

    void setFromWidth(int fromWidth) {
        mFromWidth = fromWidth;
    }

    void setToWidth(int toWidth) {
        mToWidth = toWidth;
    }

    void setFromColor(int fromColor) {
        mFromColor = fromColor;
    }

    void setToColor(int toColor) {
        mToColor = toColor;
    }

    void setFromStrokeColor(int fromStrokeColor) {
        mFromStrokeColor = fromStrokeColor;
    }

    void setToStrokeColor(int toStrokeColor) {
        mToStrokeColor = toStrokeColor;
    }

    void setFromCornerRadius(float fromCornerRadius) {
        mFromCornerRadius = fromCornerRadius;
    }

    void setToCornerRadius(float toCornerRadius) {
        mToCornerRadius = toCornerRadius;
    }

    public void setPadding(float padding) {
        mPadding = padding;
    }

    public void start() {
        ValueAnimator widthAnimation = ValueAnimator.ofInt(mFromWidth, mToWidth);
        final Drawable gradientDrawable = mDrawable.getGradientDrawable();
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                int leftOffset;
                int rightOffset;
                int padding;

                if (mFromWidth > mToWidth) {
                    leftOffset = (mFromWidth - value) / 2;
                    rightOffset = mFromWidth - leftOffset;
                    padding = (int) (mPadding * animation.getAnimatedFraction());
                } else {
                    leftOffset = (mToWidth - value) / 2;
                    rightOffset = mToWidth - leftOffset;
                    padding = (int) (mPadding - mPadding * animation.getAnimatedFraction());
                }

                gradientDrawable.setBounds(leftOffset + padding, padding, rightOffset - padding, mView.getHeight() - padding);
            }
        });

        ObjectAnimator bgColorAnimation = ObjectAnimator.ofInt(gradientDrawable, "color", mFromColor, mToColor);
        bgColorAnimation.setEvaluator(new ArgbEvaluator());

        ObjectAnimator strokeColorAnimation = ObjectAnimator.ofInt(mDrawable, "strokeColor", mFromStrokeColor, mToStrokeColor);
        strokeColorAnimation.setEvaluator(new ArgbEvaluator());

        ObjectAnimator cornerAnimation = ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", mFromCornerRadius, mToCornerRadius);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(mDuration);
        animatorSet.playTogether(widthAnimation, bgColorAnimation, strokeColorAnimation, cornerAnimation);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null) {
                    mListener.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }
}