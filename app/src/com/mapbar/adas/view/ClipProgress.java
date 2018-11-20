package com.mapbar.adas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.miyuan.obd.R;

public class ClipProgress extends View {
    private static final float SWEEP_INC = 1f;
    public float mDensity = 0;
    Bitmap mBitmap = BitmapFactory.decodeResource(this.getResources(),
            R.drawable.dashboard);
    private Paint mPaint;
    private Paint mFramePaint;
    private boolean mUseCenters;
    private RectF mBigOval;
    private float mStart = -46f;
    private int mEnd = 0;
    private float mSweep = 91f;

    public ClipProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ClipProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClipProgress(Context context) {
        super(context);
        init(context);
    }

    private void init(Context aContext) {
        mPaint = new Paint();
        mPaint.setColor(0xFF243636);
        mUseCenters = true;
        mFramePaint = new Paint();
        mFramePaint.setAntiAlias(false);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(4);
        mBigOval = new RectF(0, 0, mBitmap.getWidth(),
                mBitmap.getHeight());
    }

    private void drawArcs(Canvas canvas, RectF oval, boolean useCenter,
                          Paint paint) {
        canvas.drawArc(oval, mStart, mSweep, useCenter, paint);
    }

    public int getProgress() {
        return mEnd;
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }

        if (progress > 100) {
            progress = 100;
        }

        mEnd = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        drawArcs(canvas, mBigOval, mUseCenters, mPaint);
        if ((int) (92 - (92f / 100f * mEnd)) == mSweep) {
            return;
        }
        if (92 - (92f / 100f * mEnd) < mSweep) {
            mSweep -= SWEEP_INC;
            invalidate();
        } else {
            mSweep += SWEEP_INC;
            invalidate();
        }

    }

    public float getDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.density; // 屏幕密度（0.75 / 1.0 / 1.5）

    }
}