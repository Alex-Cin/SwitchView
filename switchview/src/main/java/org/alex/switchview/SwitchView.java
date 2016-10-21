package org.alex.switchview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
    /**
     * true = 将要打开
     * false = 将要关闭
     */
    private boolean is2Open;

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

        isCanVisibleDrawing = false;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint = new Paint();
        runwayPath = new Path();
        circleSlidePath = new Path();
        circleSlideRectF = new RectF();
        /*关联动画*/
        openAnimator = ValueAnimator.ofFloat(0f, 1.0f);
        closeAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);

        closeAnimator.setDuration(closeDuration);
        closeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        closeAnimator.addUpdateListener(new MyAnimatorUpdateListener("closeAnimator"));

        openAnimator.setDuration(openDuration);
        openAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        openAnimator.addUpdateListener(new MyAnimatorUpdateListener("openAnimator"));
        setOnClickListener(this);
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
            invalidate();
            if ("openAnimator".equals(tag)) {


            } else if ("closeAnimator".equals(tag)) {

            }
            LogUtil.e(tag + " = " + progress);
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
        //progress
        circleSlidePath.reset();
        circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
        circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
        circleSlidePath.arcTo(circleSlideRectF, 90, 180);
        circleSlideRectF.left = circleSlideLeft + circleSlideOutGap + progress * circleSlideRunLength;
        circleSlideRectF.right = circleSlideRight - circleSlideOutGap + progress * circleSlideRunLength;
        circleSlidePath.arcTo(circleSlideRectF, 270, 180);
        circleSlidePath.close();
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
        LogUtil.e("width = " + width + " height = " + height);
        LogUtil.e("circleSlideRectF left = " + circleSlideRectF.left + " top = " + circleSlideRectF.top + " right = " + circleSlideRectF.right + " bottom = " + circleSlideRectF.bottom);
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
        LogUtil.e("isOpened = " + isOpened);
        if (isOpened) {
            is2Open = false;
            closeAnimator.start();
        } else {
            is2Open = true;
            openAnimator.start();
        }
        isOpened = !isOpened;
    }
}
