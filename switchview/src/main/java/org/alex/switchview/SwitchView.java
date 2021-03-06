package org.alex.switchview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.alex.util.LogUtil;


/**
 * 作者：Alex
 * 时间：2016/10/21 12:59
 * 简述：
 */
public class SwitchView extends View implements View.OnClickListener {
    private final int defaultLightColor = 0xFF4BD763;
    private final int defaultDarkColor = 0xFFE3E3E3;
    private final int defaultCircleSlideColor = 0xFFFFFFFF;
    private final float defaultCircleScale = 0.90F;
    private final int defaultOpenDuration = 300;
    private final int defaultCloseDuration = 300;
    private Paint paint;
    /**
     * 左右两端是半圆，中间是矩形的跑道
     */
    private Path runwayPath;
    /**
     * 中间的 小滑块
     */
    private Path circleSlidePath;
    /**
     * 中间的 小滑块
     */
    private RectF circleSlideRectF;
    private int lightColor;
    private int darkColor;
    private int circleSlideColor;
    private boolean isOpened;
    private int width, height;
    private int viewBottom, viewTop, viewLeft, viewRight;
    private float runwayHeight;
    /**
     * 中间小滑块  占 滑动开关的 高度比
     */
    private float circleSlideScale;
    private float circleSlideHeight;
    private float circleSlideLeft, circleSlideTop, circleSlideRight, circleSlideBottom;

    private int openDuration, closeDuration;
    private boolean isCanVisibleDrawing;
    /**
     * 小滑块 和 外层 跑道之间的 间距
     */
    private float circleSlideOutGap;
    /**
     * 打开开关 动画
     */
    private ValueAnimator openAnimator;
    /**
     * 关闭开关 动画
     */
    private ValueAnimator closeAnimator;
    /**
     * 0 表示在最左端
     * 1 表示在最右端
     */
    private float progress;
    /**
     * 小滑块 滑动的 距离
     */
    private float circleSlideRunLength;

    private int swStatus;
    private OnChangeListener onChangeListener;

    public SwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);
        lightColor = typedArray.getColor(R.styleable.SwitchView_sv_lightColor, defaultLightColor);
        darkColor = typedArray.getColor(R.styleable.SwitchView_sv_darkColor, defaultDarkColor);
        circleSlideColor = typedArray.getColor(R.styleable.SwitchView_sv_circleSlideColor, defaultCircleSlideColor);
        circleSlideScale = typedArray.getFloat(R.styleable.SwitchView_sv_circleSlideScale, defaultCircleScale);
        openDuration = typedArray.getInt(R.styleable.SwitchView_sv_openDuration, defaultOpenDuration);
        closeDuration = typedArray.getInt(R.styleable.SwitchView_sv_closeDuration, defaultCloseDuration);
        isOpened = typedArray.getBoolean(R.styleable.SwitchView_sv_isOpened, false);
        typedArray.recycle();
        progress = isOpened ? 1F : 0F;
        swStatus = SwStatus.isNone;
        isCanVisibleDrawing = false;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint = new Paint();
        runwayPath = new Path();
        circleSlidePath = new Path();
        circleSlideRectF = new RectF();
        /*关联动画*/
        openAnimator = ValueAnimator.ofFloat(0f, 1.0f);
        closeAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        MySimpleAnimatorListener simpleAnimatorListener = new MySimpleAnimatorListener();
        closeAnimator.setDuration(closeDuration);
        closeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        closeAnimator.addUpdateListener(new MyAnimatorUpdateListener("closeAnimator"));
        closeAnimator.addListener(simpleAnimatorListener);

        openAnimator.setDuration(openDuration);
        openAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        openAnimator.addUpdateListener(new MyAnimatorUpdateListener("openAnimator"));
        openAnimator.addListener(simpleAnimatorListener);
        setOnClickListener(this);
    }

    private final class MySimpleAnimatorListener extends SimpleAnimatorListener {
        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            swStatus = SwStatus.isNone;
            LogUtil.e("isOpen = " + isOpened);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            LogUtil.e("isOpen = " + isOpened);
            if (onChangeListener != null) {
                onChangeListener.onChange(SwitchView.this, isOpened);
            }
        }
    }

    private final class MyAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private String tag;

        public MyAnimatorUpdateListener(String tag) {
            this.tag = tag;
        }

        /**
         * <p>Notifies the occurrence of another frame of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            progress = (float) animation.getAnimatedValue();
            if ("openAnimator".equals(tag)) {
                if (progress > 0.99F) {
                    swStatus = SwStatus.isNone;
                    isOpened = true;
                } else {
                    swStatus = SwStatus.is2Open;
                }
            } else if ("closeAnimator".equals(tag)) {
                if (progress < 0.01F) {
                    swStatus = SwStatus.isNone;
                    isOpened = false;
                } else {
                    swStatus = SwStatus.is2Close;
                }
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isCanVisibleDrawing) {
            return;
        }
        paint.setAntiAlias(true);
        final boolean isOn = isOpened;
        /*画背景*/
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isOn ? lightColor : darkColor);
        canvas.drawPath(runwayPath, paint);
        //canvas.save();

        /*画 中间的 圆形的 滑块*/
        //canvas.scale(circleSlideScale, circleSlideScale, circleSlideHeight / 2, circleSlideHeight / 2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(circleSlideColor);
        calcCircleSlidePath();
        canvas.drawPath(circleSlidePath, paint);

    }

    private void calcCircleSlidePath() {
        circleSlidePath.reset();
        if (swStatus == SwStatus.is2Open) {
            slowly4Open();
        } else if (swStatus == SwStatus.is2Close) {
            slowly4Close();
        } else {
            circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
            circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
            circleSlidePath.arcTo(circleSlideRectF, 90, 180);
            circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
            circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
            circleSlidePath.arcTo(circleSlideRectF, 270, 180);
        }
        circleSlidePath.close();
    }

    private void slowly4Open() {
        circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
        circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
        circleSlidePath.arcTo(circleSlideRectF, 90, 180);
        if (progress < 0.8) {
            circleSlideRectF.right = circleSlideRight - circleSlideOutGap + (progress + 0.2F) * circleSlideRunLength;
        } else if ((progress >= 0.8) && (progress < 0.99)) {
            circleSlideRectF.right = circleSlideRight - circleSlideOutGap + (progress + (1 - progress)) * circleSlideRunLength;
        } else {
            circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
            circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
        }
        circleSlidePath.arcTo(circleSlideRectF, -90, 180);
    }

    private void slowly4Close() {
        circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
        circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
        circleSlidePath.arcTo(circleSlideRectF, -90, 180);

        if (progress > 0.2) {
            circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + (progress - 0.2F) * circleSlideRunLength;
        } else if ((progress <= 0.2) && (progress > 0.01)) {
            circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + (progress * 0.2F) * circleSlideRunLength;
        } else {
            circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
            circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
        }
        circleSlidePath.arcTo(circleSlideRectF, 90, 180);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        isCanVisibleDrawing = width > getPaddingLeft() + getPaddingRight() && height > getPaddingTop() + getPaddingBottom();
        if (!isCanVisibleDrawing) {
            return;
        }
        viewLeft = getPaddingLeft();
        viewRight = getWidth() - getPaddingRight();
        viewTop = getPaddingTop();
        viewBottom = getHeight() - getPaddingBottom();
        runwayHeight = viewBottom - viewTop;

        circleSlideLeft = viewLeft;
        circleSlideTop = viewTop;
        circleSlideBottom = viewBottom;
        circleSlideHeight = viewBottom - viewTop;
        circleSlideRight = viewLeft + circleSlideHeight;

        runwayPath.reset();
        /*画跑道需要的矩形*/
        RectF runwayRectF = new RectF();
        runwayRectF.top = viewTop;
        runwayRectF.bottom = viewBottom;
        runwayRectF.left = viewLeft;
        runwayRectF.right = viewLeft + runwayHeight;
        /*画 跑道的左半 半圆*/
        runwayPath.arcTo(runwayRectF, 90, 180);
        runwayRectF.left = viewRight - runwayHeight;
        runwayRectF.right = viewRight;
        /*画 跑道的右半 半圆*/
        runwayPath.arcTo(runwayRectF, -90, 180);
        runwayPath.close();

        circleSlideOutGap = height * (1 - circleSlideScale) * 0.5F;
        circleSlideRectF.left = circleSlideLeft + circleSlideOutGap;
        circleSlideRectF.right = circleSlideRight - circleSlideOutGap;
        circleSlideRectF.top = circleSlideTop + circleSlideOutGap;
        circleSlideRectF.bottom = circleSlideBottom - circleSlideOutGap;

        circleSlideRunLength = width - circleSlideOutGap * 2 - (circleSlideRectF.right - circleSlideRectF.left);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int resultWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            resultWidth = widthSize;
        } else {
            resultWidth = (int) (56 * getResources().getDisplayMetrics().density + 0.5f) + getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                resultWidth = Math.min(resultWidth, widthSize);
            }
        }

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int resultHeight;
        if (heightMode == MeasureSpec.EXACTLY) {
            resultHeight = heightSize;
        } else {
            int selfExpectedResultHeight = (int) (resultWidth * 0.68F) + getPaddingTop() + getPaddingBottom();
            resultHeight = selfExpectedResultHeight;
            if (heightMode == MeasureSpec.AT_MOST) {
                resultHeight = Math.min(resultHeight, heightSize);
            }
        }
        setMeasuredDimension(resultWidth, resultHeight);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (closeAnimator.isRunning() || openAnimator.isRunning()) {
            return;
        }
        if (isOpened) {
            swStatus = SwStatus.is2Close;
            closeAnimator.start();
        } else {
            swStatus = SwStatus.is2Open;
            openAnimator.start();
        }
    }

    /**
     * 开关 打开的 颜色
     */
    public SwitchView lightColor(String lightColor) {
        return lightColor(Color.parseColor(lightColor));
    }

    /**
     * 开关 打开的 颜色
     */
    public SwitchView lightColor(int lightColor) {
        this.lightColor = lightColor;
        invalidate();
        return this;
    }

    /**
     * 开关 关闭的 颜色
     */
    public SwitchView darkColor(String darkColor) {
        return darkColor(Color.parseColor(darkColor));
    }

    /**
     * 开关 关闭的 颜色
     */
    public SwitchView darkColor(int darkColor) {
        this.darkColor = darkColor;
        invalidate();
        return this;
    }

    /**
     * 开关 关闭的 周期
     */
    public SwitchView closeDuration(int closeDuration) {
        this.closeDuration = closeDuration;
        return this;
    }

    /**
     * 开关 打开的 周期
     */
    public SwitchView openDuration(int openDuration) {
        this.openDuration = openDuration;
        return this;
    }

    /**
     * 打开的状态
     */
    public SwitchView isOpened(boolean isOpened) {
        this.isOpened = isOpened;
        invalidate();
        return this;
    }

    /**
     * 中间的 小滑块 占据 外面跑道的 高度比
     */
    public SwitchView circleSlideScale(float circleSlideScale) {
        this.circleSlideScale = circleSlideScale;
        invalidate();
        return this;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public SwitchView open() {
        cancelAnim();
        isOpened = true;
        progress = 1.0F;
        if (onChangeListener != null) {
            onChangeListener.onChange(this, true);
        }
        invalidate();
        return this;
    }

    public SwitchView close() {
        cancelAnim();
        isOpened = false;
        progress = 0.0F;
        if (onChangeListener != null) {
            onChangeListener.onChange(this, false);
        }
        invalidate();
        return this;
    }

    private void cancelAnim() {
        if (openAnimator != null) {
            openAnimator.cancel();
        }
        if (closeAnimator != null) {
            closeAnimator.cancel();
        }
    }

    public SwitchView onChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
        return this;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.isOpened = isOpened;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.isOpened = savedState.isOpened;
        this.swStatus = SwStatus.isNone;
        invalidate();
    }

    @SuppressLint("ParcelCreator")
    static final class SavedState extends BaseSavedState {
        private boolean isOpened;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isOpened = 1 == in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isOpened ? 1 : 0);
        }
    }

    public interface OnChangeListener {
        void onChange(SwitchView switchView, boolean isOpened);
    }


    private final class SwStatus {
        /**
         * 初始状态
         */
        private final static int isNone = 0;
        /**
         * 初始状态
         */
        private final static int is2Open = 1;
        /**
         * 初始状态
         */
        private final static int is2Close = 2;
    }
}
