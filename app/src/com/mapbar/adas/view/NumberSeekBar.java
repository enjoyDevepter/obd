package com.mapbar.adas.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.mapbar.adas.GlobalUtil;
import com.mapbar.adas.LayoutUtils;
import com.miyuan.obd.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by shisk on 2017/7/14.
 */

public class NumberSeekBar extends View {
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = -1;

    /**
     * Position of the last motion event.
     */
    private float mLastMotionX;
    private Paint textPaint;
    private Paint bottomTextPaint;
    private Paint progressPaint;
    private Paint thumbPaint;
    private Paint thumbPaintBig;
    private RectF progressRect;
    private float thumbCx;
    private float thumbCy;

    private int minProgress;
    private int maxProgress;

    private int curProgress;
    //滑动中每像素所带票的滑动值
    private double unint;

    private GestureDetector detector;

    private OnProgressChangeListener onProgressChangeListener;
    private RectF dragRegion = new RectF();
    //当前滑动块左边的位置
    private double currentIndex;
    private int circleHeight;
    private int circleInnerHeight;
    private float topTexBaseLineY, bottomTexBaseLineY;
    private float textWid;
    private float delatx;
    private int height, width;
    //第一次按下是否在可拖动区域，防止第一次在区域 活动的时候 出区域了，不在进行滑动
    private boolean isHasDown;

    public NumberSeekBar(Context context) {
        super(context);
    }

    public NumberSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NumberSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    private void setCurrentIndex(double currentIndex) {
        thumbCx = (float) (currentIndex + circleInnerHeight / 2);
        invalidate();
    }

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.NumberSeekBar);
        minProgress = array.getInt(R.styleable.NumberSeekBar_min_point, 0);
        maxProgress = array.getInt(R.styleable.NumberSeekBar_max_point, 100);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(LayoutUtils.getPxByDimens(R.dimen.textSize30));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(getResources().getColor(R.color.seekbar_textcolor_light));

        bottomTextPaint = new TextPaint();
        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setTextSize(LayoutUtils.getPxByDimens(R.dimen.textSize30));
        bottomTextPaint.setStyle(Paint.Style.FILL);
        bottomTextPaint.setTextAlign(Paint.Align.LEFT);
        bottomTextPaint.setColor(getResources().getColor(R.color.seekbar_textcolor_red));

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(getResources().getColor(R.color.seekbar_progress));
        progressPaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);
        thumbPaint.setARGB(204, 0, 255, 244);
        thumbPaint.setStyle(Paint.Style.FILL);

        thumbPaintBig = new Paint();
        thumbPaintBig.setAntiAlias(true);
        thumbPaintBig.setARGB(102, 0, 255, 244);
        thumbPaintBig.setStyle(Paint.Style.FILL);


        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textWid = textPaint.measureText("km/h", 0, 4);
        currentIndex = textWid;
        float textHeight = Math.abs(fontMetrics.bottom) + Math.abs(fontMetrics.top);
        int padding = LayoutUtils.getPxByDimens(R.dimen.padding_10);
        circleHeight = LayoutUtils.getPxByDimens(R.dimen.size_40);

        int height = (int) textHeight * 2 + padding * 2 + circleHeight;
        int width = LayoutUtils.getPxByDimens(R.dimen.size_456);
        int progressHeight = LayoutUtils.getPxByDimens(R.dimen.size_14);
        circleInnerHeight = LayoutUtils.getPxByDimens(R.dimen.size_22);

        this.width = (int) (width + textWid * 2);
        this.height = height;

        int top = (height - progressHeight) / 2;
        int bottom = top + progressHeight;
        int left = (int) textWid;
        int right = (int) (this.width - textWid);
        //  int bottom=
        progressRect = new RectF(left, top, right, bottom);

        //progress 的值区间长度
        int progressMaxValues = maxProgress - minProgress;
        //可滑动的区域值区间长度
        float scrollWith = this.width - textWid * 2 - circleInnerHeight;
        //滑动区域中每刻度滑动条代表的滑动的值
        BigDecimal bd = new BigDecimal(Double.toString(progressMaxValues));
        BigDecimal bd2 = new BigDecimal(Double.toString(scrollWith));
        BigDecimal unint = bd.divide(bd2, 6, RoundingMode.UP);
        this.unint = unint.doubleValue();
        thumbCx = (float) (currentIndex + circleInnerHeight / 2);
        thumbCy = this.height / 2;

        topTexBaseLineY = progressRect.top - padding - Math.abs(fontMetrics.bottom);
        bottomTexBaseLineY = progressRect.bottom + padding + Math.abs(bottomTextPaint.getFontMetrics().top);

        detector = new GestureDetector(GlobalUtil.getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (!dragRegion.contains((int) e.getX(), (int) e.getY())) {
                    if (mLastMotionX > progressRect.width() + textWid) {//progressRect.width()+textWid,进度条在View中的最右边的位置
                        currentIndex = (int) (progressRect.width() + textWid - circleInnerHeight);
                    } else if (mLastMotionX < textWid) {//最小值
                        currentIndex = textWid;
                    } else {
                        currentIndex = mLastMotionX;
                    }

                    setCurrentIndex(currentIndex);
                    calculateProgress();
                    invalidate();
                }
                return true;
            }


            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                if (!dragRegion.contains((int) e1.getX(), (int) e1.getY()) && !isHasDown) {
                    currentIndex = thumbCx - circleInnerHeight / 2;
                    return true;
                }
                float dX = 0 - distanceX;
                delatx = dX;

                //防止画出屏幕外
                if (currentIndex + dX < textWid) {
                    currentIndex = textWid;
                } else if (currentIndex + circleInnerHeight + dX > getMeasuredWidth() - textWid) {
                    currentIndex = getMeasuredWidth() - textWid - circleInnerHeight;
                } else {
                    currentIndex = (float) currentIndex + dX;
                }
                setCurrentIndex(currentIndex);
                calculateProgress();
                invalidate();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }


            @Override
            public boolean onDown(MotionEvent e) {
                mActivePointerId = e.getPointerId(0);
                mLastMotionX = e.getX(mActivePointerId);
                if (dragRegion.contains((int) e.getX(), (int) e.getY())) {
                    currentIndex = thumbCx - circleInnerHeight / 2;
                    isHasDown = true;
                } else {
                    isHasDown = false;
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (onProgressChangeListener != null) {
                onProgressChangeListener.onProgress(curProgress);
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        dragRegion.left = thumbCx - circleHeight * 2;
        dragRegion.top = thumbCy - circleHeight * 2;
        dragRegion.bottom = thumbCy + circleHeight * 2;
        dragRegion.right = thumbCx + circleHeight * 2;

        canvas.drawRoundRect(progressRect, 12, 12, progressPaint);
        canvas.drawCircle(thumbCx, thumbCy, circleInnerHeight / 2, thumbPaint);
        canvas.drawCircle(thumbCx, thumbCy, circleHeight / 2, thumbPaintBig);
        canvas.drawText(curProgress + "", thumbCx, topTexBaseLineY, textPaint);
        bottomTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("最低" + "", progressRect.left, bottomTexBaseLineY, bottomTextPaint);
        bottomTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("最高" + "", progressRect.right, bottomTexBaseLineY, bottomTextPaint);
    }

    public int getCurProgress() {
        return curProgress;
    }

    public void setCurProgress(int curProgress) {
        this.curProgress = curProgress;
        currentIndex = (float) ((curProgress - minProgress) / unint) + progressRect.left;
        if (currentIndex > this.width - textWid - circleInnerHeight) {//预防设置的值过大或者过小导致画出边界
            currentIndex = this.width - textWid - circleInnerHeight;
        } else if (currentIndex < textWid) {
            currentIndex = textWid;
        }
        setCurrentIndex(currentIndex);
    }

    /**
     *
     */
    private void calculateProgress() {
        //防止画出屏幕外
        if (currentIndex + delatx < textWid) {
            curProgress = minProgress;
        } else if (currentIndex + circleInnerHeight + delatx >= this.width - textWid) {
            curProgress = maxProgress;
        } else {
            curProgress = (int) (((thumbCx - circleInnerHeight / 2 - textWid) * unint) + minProgress);
        }
    }

    /**
     * 获取seekbar 距离左左边的边距
     *
     * @return
     */
    public int getPaddingLeft() {
        return (int) textWid;
    }

    public interface OnProgressChangeListener {
        void onProgress(int progress);
    }
}
