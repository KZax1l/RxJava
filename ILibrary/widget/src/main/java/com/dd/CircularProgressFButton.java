package com.dd;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import org.zsago.widget.R;

import static com.dd.processbutton.OsCompat.getResourceColorCompat;
import static com.dd.processbutton.OsCompat.getResourceDrawableCompat;
import static com.dd.processbutton.OsCompat.setBackgroundDrawableCompat;

public class CircularProgressFButton extends Button implements View.OnTouchListener {

    public static final int IDLE_STATE_PROGRESS = 0;
    public static final int ERROR_STATE_PROGRESS = -1;

    private StrokeGradientDrawable background;
    private CircularAnimatedDrawable mAnimatedDrawable;
    private CircularProgressDrawable mProgressDrawable;

    private int mIdleColor;// Default Color
    private int mCompleteColor;// Pressed Color
    private int mErrorColor;// Error Color

    private StateManager mStateManager;
    private State mState;
    private String mIdleText;
    private String mCompleteText;
    private String mErrorText;
    private String mProgressText;

    private int mColorProgress;
    private int mColorIndicator;
    private int mColorIndicatorBackground;
    private int mIconComplete;
    private int mIconError;
    private int mStrokeWidth;
    private int mPaddingProgress;
    private int mCornerRadius;
    private boolean mIndeterminateProgressMode;
    private boolean mConfigurationChanged;

    //Custom values
    private boolean isShadowEnabled = true;
    private int mButtonColor;
    private int mShadowColor;
    private int mDisableColor;
    private int mShadowHeight;
    //Native values
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;
    //Background drawable
    private LayerDrawable pressedDrawable;
    private LayerDrawable unpressedDrawable;

    boolean isShadowColorDefined = false;

    private enum State {
        PROGRESS, IDLE, COMPLETE, ERROR
    }

    private int mMaxProgress;
    private int mProgress;

    private boolean mMorphingInProgress;

    public CircularProgressFButton(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgressFButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressFButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mStrokeWidth = (int) getDimension(R.dimen.cpb_stroke_width);
        initFButtonAttrs(context, attributeSet);
        initAttributes(context, attributeSet);
        mMaxProgress = 100;
        mState = State.IDLE;
        mStateManager = new StateManager(this);
        setText(mIdleText);
        setBackgroundCompat(initIdleStateDrawable());
    }

    private Drawable initErrorStateDrawable() {
        setButtonColor(mErrorColor);
        return getPressedDrawable();
    }

    private Drawable initCompleteStateDrawable() {
        setButtonColor(mCompleteColor);
        return getPressedDrawable();
    }

    private Drawable initIdleStateDrawable() {
        setButtonColor(mIdleColor);
        LayerDrawable idleDrawable = getDefaultDrawable();
        if (background == null) {
            Drawable gradientDrawable = idleDrawable.getDrawable(0);
            if (gradientDrawable instanceof GradientDrawable) {
                background = new StrokeGradientDrawable((GradientDrawable) gradientDrawable);
            }
        }
        return idleDrawable;
    }

    @Override
    protected void drawableStateChanged() {
        if (mState == State.COMPLETE) {
            setBackgroundCompat(initCompleteStateDrawable());
        } else if (mState == State.IDLE) {
            setBackgroundCompat(initIdleStateDrawable());
        } else if (mState == State.ERROR) {
            setBackgroundCompat(initErrorStateDrawable());
        }
        if (mState != State.PROGRESS) {
            super.drawableStateChanged();
        }
    }

    private void initFButtonAttrs(Context context, AttributeSet attrs) {
        //Init default values
        isShadowEnabled = true;
        Resources resources = getResources();
        if (resources == null) return;
        mButtonColor = getColor(R.color.fbutton_default_color);
        mShadowColor = getColor(R.color.fbutton_default_shadow_color);
        mShadowHeight = resources.getDimensionPixelSize(R.dimen.fbutton_default_shadow_height);
        mCornerRadius = resources.getDimensionPixelSize(R.dimen.fbutton_default_conner_radius);

        //Load from custom attributes
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressFButton);
        if (typedArray == null) return;
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.CircularProgressFButton_cpfb_shadowEnabled) {
                isShadowEnabled = typedArray.getBoolean(attr, true); //Default is true
            } else if (attr == R.styleable.CircularProgressFButton_cpfb_buttonColor) {
                mButtonColor = typedArray.getColor(attr, getColor(R.color.fbutton_default_color));
            } else if (attr == R.styleable.CircularProgressFButton_cpfb_shadowColor) {
                mShadowColor = typedArray.getColor(attr, getColor(R.color.fbutton_default_shadow_color));
                isShadowColorDefined = true;
            } else if (attr == R.styleable.CircularProgressFButton_cpfb_disableColor) {
                mDisableColor = typedArray.getColor(attr, getColor(R.color.fbutton_default_disable_color));
            } else if (attr == R.styleable.CircularProgressFButton_cpfb_shadowHeight) {
                mShadowHeight = typedArray.getDimensionPixelSize(attr, (int) getDimension(R.dimen.fbutton_default_shadow_height));
            }
        }
        typedArray.recycle();

        //Get paddingLeft,paddingRight,paddingTop,paddingBottom
        int[] attrsArray = new int[]{
                android.R.attr.paddingLeft,  // 0
                android.R.attr.paddingRight, // 1
                android.R.attr.paddingTop,   // 2
                android.R.attr.paddingBottom // 3
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
        if (ta == null) return;
        mPaddingLeft = ta.getDimensionPixelSize(0, 0);
        mPaddingRight = ta.getDimensionPixelSize(1, 0);
        mPaddingTop = ta.getDimensionPixelSize(2, 0);
        mPaddingBottom = ta.getDimensionPixelSize(3, 0);
        ta.recycle();
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.CircularProgressButton);
        if (attr == null) {
            return;
        }
        try {
            mIdleText = attr.getString(R.styleable.CircularProgressFButton_cpfb_textIdle);
            mCompleteText = attr.getString(R.styleable.CircularProgressFButton_cpfb_textComplete);
            mErrorText = attr.getString(R.styleable.CircularProgressFButton_cpfb_textError);
            mProgressText = attr.getString(R.styleable.CircularProgressFButton_cpfb_textProgress);
            mIconComplete = attr.getResourceId(R.styleable.CircularProgressFButton_cpfb_iconComplete, 0);
            mIconError = attr.getResourceId(R.styleable.CircularProgressFButton_cpfb_iconError, 0);
            mCornerRadius = (int) attr.getDimension(R.styleable.CircularProgressFButton_cpfb_cornerRadius, 0);
            mPaddingProgress = attr.getDimensionPixelSize(R.styleable.CircularProgressFButton_cpfb_paddingProgress, 0);

            int blue = getColor(R.color.cpb_blue);
            int white = getColor(R.color.cpb_white);
            int grey = getColor(R.color.cpb_grey);

            mIdleColor = attr.getColor(R.styleable.CircularProgressFButton_cpfb_idleColor, getColor(R.color.cpb_idle_state_selector));
            mCompleteColor = attr.getColor(R.styleable.CircularProgressFButton_cpfb_completeColor, getColor(R.color.cpb_complete_state_selector));
            mErrorColor = attr.getColor(R.styleable.CircularProgressFButton_cpfb_errorColor, getColor(R.color.cpb_error_state_selector));
            mColorProgress = attr.getColor(R.styleable.CircularProgressFButton_cpfb_colorProgress, white);
            mColorIndicator = attr.getColor(R.styleable.CircularProgressFButton_cpfb_colorIndicator, blue);
            mColorIndicatorBackground = attr.getColor(R.styleable.CircularProgressFButton_cpfb_colorIndicatorBackground, grey);
        } finally {
            attr.recycle();
        }
    }

    protected TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mProgress > 0 && mState == State.PROGRESS && !mMorphingInProgress) {
            if (mIndeterminateProgressMode) {
                drawIndeterminateProgress(canvas);
            } else {
                drawProgress(canvas);
            }
        }
    }

    private void drawIndeterminateProgress(Canvas canvas) {
        if (mAnimatedDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            mAnimatedDrawable = new CircularAnimatedDrawable(mColorIndicator, mStrokeWidth);
            int left = offset + mPaddingProgress;
            int right = getWidth() - offset - mPaddingProgress;
            int bottom = getHeight() - mPaddingProgress;
            int top = mPaddingProgress;
            mAnimatedDrawable.setBounds(left, top, right, bottom);
            mAnimatedDrawable.setCallback(this);
            mAnimatedDrawable.start();
        } else {
            mAnimatedDrawable.draw(canvas);
        }
    }

    private void drawProgress(Canvas canvas) {
        if (mProgressDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            int size = getHeight() - mPaddingProgress * 2;
            mProgressDrawable = new CircularProgressDrawable(size, mStrokeWidth, mColorIndicator);
            int left = offset + mPaddingProgress;
            mProgressDrawable.setBounds(left, mPaddingProgress, left, mPaddingProgress);
        }
        float sweepAngle = (360f / mMaxProgress) * mProgress;
        mProgressDrawable.setSweepAngle(sweepAngle);
        mProgressDrawable.draw(canvas);
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mAnimatedDrawable || super.verifyDrawable(who);
    }

    private MorphingAnimation createMorphing() {
        mMorphingInProgress = true;
        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(mCornerRadius);
        animation.setToCornerRadius(mCornerRadius);
        animation.setFromWidth(getWidth());
        animation.setToWidth(getWidth());
        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL);
        }
        mConfigurationChanged = false;
        return animation;
    }

    private MorphingAnimation createProgressMorphing(float fromCorner, float toCorner, int fromWidth, int toWidth) {
        mMorphingInProgress = true;
        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(fromCorner);
        animation.setToCornerRadius(toCorner);
        animation.setPadding(mPaddingProgress);
        animation.setFromWidth(fromWidth);
        animation.setToWidth(toWidth);
        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL);
        }
        mConfigurationChanged = false;
        return animation;
    }

    private void morphToProgress() {
        setWidth(getWidth());
        setText(mProgressText);
        MorphingAnimation animation = createProgressMorphing(mCornerRadius, getHeight(), getWidth(), getHeight());
        animation.setFromColor(mIdleColor);
        animation.setToColor(mColorProgress);
        animation.setFromStrokeColor(mIdleColor);
        animation.setToStrokeColor(mColorIndicatorBackground);
        animation.setListener(mProgressStateListener);
        animation.start();
    }

    private OnAnimationEndListener mProgressStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            mMorphingInProgress = false;
            mState = State.PROGRESS;
            mStateManager.checkState(CircularProgressFButton.this);
        }
    };

    private void morphProgressToComplete() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());
        animation.setFromColor(mColorProgress);
        animation.setToColor(mCompleteColor);
        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(mCompleteColor);
        animation.setListener(mCompleteStateListener);
        animation.start();
    }

    private void morphIdleToComplete() {
        MorphingAnimation animation = createMorphing();
        animation.setFromColor(mIdleColor);
        animation.setToColor(mCompleteColor);
        animation.setFromStrokeColor(mIdleColor);
        animation.setToStrokeColor(mCompleteColor);
        animation.setListener(mCompleteStateListener);
        animation.start();
    }

    private OnAnimationEndListener mCompleteStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            if (mIconComplete != 0) {
                setText(null);
                setIcon(mIconComplete);
            } else {
                setText(mCompleteText);
            }
            mMorphingInProgress = false;
            mState = State.COMPLETE;
            mStateManager.checkState(CircularProgressFButton.this);
        }
    };

    private void morphCompleteToIdle() {
        MorphingAnimation animation = createMorphing();
        animation.setFromColor(mCompleteColor);
        animation.setToColor(mIdleColor);
        animation.setFromStrokeColor(mCompleteColor);
        animation.setToStrokeColor(mIdleColor);
        animation.setListener(mIdleStateListener);
        animation.start();
    }

    private void morphErrorToIdle() {
        MorphingAnimation animation = createMorphing();
        animation.setFromColor(mErrorColor);
        animation.setToColor(mIdleColor);
        animation.setFromStrokeColor(mErrorColor);
        animation.setToStrokeColor(mIdleColor);
        animation.setListener(mIdleStateListener);
        animation.start();
    }

    private OnAnimationEndListener mIdleStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            removeIcon();
            setText(mIdleText);
            mMorphingInProgress = false;
            mState = State.IDLE;
            mStateManager.checkState(CircularProgressFButton.this);
        }
    };

    private void morphIdleToError() {
        MorphingAnimation animation = createMorphing();
        animation.setFromColor(mIdleColor);
        animation.setToColor(mErrorColor);
        animation.setFromStrokeColor(mIdleColor);
        animation.setToStrokeColor(mErrorColor);
        animation.setListener(mErrorStateListener);
        animation.start();
    }

    private void morphProgressToError() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());
        animation.setFromColor(mColorProgress);
        animation.setToColor(mErrorColor);
        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(mErrorColor);
        animation.setListener(mErrorStateListener);
        animation.start();
    }

    private OnAnimationEndListener mErrorStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            if (mIconError != 0) {
                setText(null);
                setIcon(mIconError);
            } else {
                setText(mErrorText);
            }
            mMorphingInProgress = false;
            mState = State.ERROR;
            mStateManager.checkState(CircularProgressFButton.this);
        }
    };

    private void morphProgressToIdle() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());
        animation.setFromColor(mColorProgress);
        animation.setToColor(mIdleColor);
        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(mIdleColor);
        animation.setListener(new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                removeIcon();
                setText(mIdleText);
                mMorphingInProgress = false;
                mState = State.IDLE;
                mStateManager.checkState(CircularProgressFButton.this);
            }
        });
        animation.start();
    }

    private void setIcon(int icon) {
        Drawable drawable = getDrawable(icon);
        if (drawable != null) {
            int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            setPadding(padding, 0, 0, 0);
        }
    }

    protected void removeIcon() {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        setPadding(0, 0, 0, 0);
    }

    public void setProgress(int progress) {
        mProgress = progress;
        if (mMorphingInProgress || getWidth() == 0) {
            return;
        }
        mStateManager.saveProgress(this);
        if (mProgress >= mMaxProgress) {
            if (mState == State.PROGRESS) {
                morphProgressToComplete();
            } else if (mState == State.IDLE) {
                morphIdleToComplete();
            }
        } else if (mProgress > IDLE_STATE_PROGRESS) {
            if (mState == State.IDLE) {
                morphToProgress();
            } else if (mState == State.PROGRESS) {
                invalidate();
            }
        } else if (mProgress == ERROR_STATE_PROGRESS) {
            if (mState == State.PROGRESS) {
                morphProgressToError();
            } else if (mState == State.IDLE) {
                morphIdleToError();
            }
        } else if (mProgress == IDLE_STATE_PROGRESS) {
            if (mState == State.COMPLETE) {
                morphCompleteToIdle();
            } else if (mState == State.PROGRESS) {
                morphProgressToIdle();
            } else if (mState == State.ERROR) {
                morphErrorToIdle();
            }
        }
    }

    public void refresh() {
        int alpha = Color.alpha(mButtonColor);
        float[] hsv = new float[3];
        Color.colorToHSV(mButtonColor, hsv);
        hsv[2] *= 0.8f; // value component
        //if shadow color was not defined, generate shadow color = 80% brightness
        if (!isShadowColorDefined) {
            mShadowColor = Color.HSVToColor(alpha, hsv);
        }
        //Create pressed background and unpressed background drawables
        if (this.isEnabled()) {
            if (isShadowEnabled) {
                pressedDrawable = createDrawable(mCornerRadius, Color.TRANSPARENT, mButtonColor);
                unpressedDrawable = createDrawable(mCornerRadius, mButtonColor, mShadowColor);
            } else {
                mShadowHeight = 0;
                pressedDrawable = createDrawable(mCornerRadius, mShadowColor, Color.TRANSPARENT);
                unpressedDrawable = createDrawable(mCornerRadius, mButtonColor, Color.TRANSPARENT);
            }
        } else {
            if (mDisableColor == Color.TRANSPARENT) {
                Color.colorToHSV(mButtonColor, hsv);
                hsv[1] *= 0.25f; // saturation component
                mDisableColor = mShadowColor = Color.HSVToColor(alpha, hsv);
            }
            // Disabled button does not have shadow
            pressedDrawable = createDrawable(mCornerRadius, mDisableColor, Color.TRANSPARENT);
            unpressedDrawable = createDrawable(mCornerRadius, mDisableColor, Color.TRANSPARENT);
        }
        updateBackground(unpressedDrawable);
        //Set padding
        this.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom + mShadowHeight);
    }

    private void updateBackground(Drawable background) {
        if (background == null) return;
        setBackgroundCompat(background);
    }

    private LayerDrawable createDrawable(int radius, int topColor, int bottomColor) {
        //Top
        GradientDrawable topGradientDrawable = new GradientDrawable();
        topGradientDrawable.setCornerRadius(radius);
        topGradientDrawable.setColor(topColor);
        //Bottom
        GradientDrawable bottomGradientDrawable = new GradientDrawable();
        bottomGradientDrawable.setCornerRadius(radius);
        bottomGradientDrawable.setColor(bottomColor);
        //Create array
        GradientDrawable[] drawArray = {bottomGradientDrawable, topGradientDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(drawArray);
        //Set shadow height
        if (isShadowEnabled && topColor != Color.TRANSPARENT) {
            //unpressed drawable
            layerDrawable.setLayerInset(0, 0, 0, 0, 0);  /*index, left, top, right, bottom*/
        } else {
            //pressed drawable
            layerDrawable.setLayerInset(0, 0, mShadowHeight, 0, 0);  /*index, left, top, right, bottom*/
        }
        layerDrawable.setLayerInset(1, 0, 0, 0, mShadowHeight);  /*index, left, top, right, bottom*/
        return layerDrawable;
    }

    public int getProgress() {
        return mProgress;
    }

    public String getIdleText() {
        return mIdleText;
    }

    public String getCompleteText() {
        return mCompleteText;
    }

    public String getErrorText() {
        return mErrorText;
    }

    public void setIdleText(String text) {
        mIdleText = text;
    }

    public void setCompleteText(String text) {
        mCompleteText = text;
    }

    public void setErrorText(String text) {
        mErrorText = text;
    }

    public boolean isIndeterminateProgressMode() {
        return mIndeterminateProgressMode;
    }

    public void setIndeterminateProgressMode(boolean indeterminateProgressMode) {
        this.mIndeterminateProgressMode = indeterminateProgressMode;
    }

    protected float getDimension(int id) {
        return getResources().getDimension(id);
    }

    protected int getColor(int id) {
        return getResourceColorCompat(this, id);
    }

    public void setButtonColor(int buttonColor) {
        this.mButtonColor = buttonColor;
        refresh();
    }

    public int getButtonColor() {
        return mButtonColor;
    }

    protected Drawable getDrawable(int id) {
        return getResourceDrawableCompat(this, id);
    }

    public LayerDrawable getPressedDrawable() {
        return pressedDrawable;
    }

    public LayerDrawable getDefaultDrawable() {
        return unpressedDrawable;
    }

    public void setBackgroundCompat(Drawable drawable) {
        int pL = getPaddingLeft();
        int pT = getPaddingTop();
        int pR = getPaddingRight();
        int pB = getPaddingBottom();
        setBackgroundDrawableCompat(this, drawable);
        setPadding(pL, pT, pR, pB);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Update background color
        refresh();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            setProgress(mProgress);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;
        savedState.mIndeterminateProgressMode = mIndeterminateProgressMode;
        savedState.mConfigurationChanged = true;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mProgress = savedState.mProgress;
            mIndeterminateProgressMode = savedState.mIndeterminateProgressMode;
            mConfigurationChanged = savedState.mConfigurationChanged;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(mProgress);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                updateBackground(pressedDrawable);
                this.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom);
                break;
            case MotionEvent.ACTION_MOVE:
                Rect r = new Rect();
                view.getLocalVisibleRect(r);
                if (!r.contains((int) motionEvent.getX(), (int) motionEvent.getY() + 3 * mShadowHeight) &&
                        !r.contains((int) motionEvent.getX(), (int) motionEvent.getY() - 3 * mShadowHeight)) {
                    updateBackground(unpressedDrawable);
                    this.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom + mShadowHeight);
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                updateBackground(unpressedDrawable);
                this.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom + mShadowHeight);
                break;
        }
        return false;
    }

    static class SavedState extends BaseSavedState {
        private boolean mIndeterminateProgressMode;
        private boolean mConfigurationChanged;
        private int mProgress;

        SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
            mIndeterminateProgressMode = in.readInt() == 1;
            mConfigurationChanged = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
            out.writeInt(mIndeterminateProgressMode ? 1 : 0);
            out.writeInt(mConfigurationChanged ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
