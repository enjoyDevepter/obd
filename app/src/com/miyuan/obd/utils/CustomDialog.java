package com.miyuan.obd.utils;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentManager;
import android.view.View;

/**
 * Created by guomin on 2018/3/12.
 */

public class CustomDialog extends BaseDialog {

    private static final String KEY_LAYOUT_RES = "bottom_layout_res";
    private static final String KEY_HEIGHT = "bottom_height";
    private static final String KEY_WIDTH = "bottom_width";
    private static final String KEY_DIM = "bottom_dim";
    private static final String KEY_CENTER = "bottom_center";
    private static final String KEY_CANCEL_OUTSIDE = "bottom_cancel_outside";

    private FragmentManager mFragmentManager;

    private boolean mIsCancelOutside = super.getCancelOutside();

    private String mTag = super.getFragmentTag();

    private float mDimAmount = super.getDimAmount();

    private int mHeight = super.getHeight();

    private int mWidth = super.getWidth();

    private boolean mCenter = super.isCenter();

    @LayoutRes
    private int mLayoutRes;

    private ViewListener mViewListener;

    public static CustomDialog create(FragmentManager manager) {
        CustomDialog dialog = new CustomDialog();
        dialog.setFragmentManager(manager);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLayoutRes = savedInstanceState.getInt(KEY_LAYOUT_RES);
            mHeight = savedInstanceState.getInt(KEY_HEIGHT);
            mDimAmount = savedInstanceState.getFloat(KEY_DIM);
            mIsCancelOutside = savedInstanceState.getBoolean(KEY_CANCEL_OUTSIDE);
            mWidth = savedInstanceState.getInt(KEY_WIDTH);
            mCenter = savedInstanceState.getBoolean(KEY_CENTER);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_LAYOUT_RES, mLayoutRes);
        outState.putInt(KEY_HEIGHT, mHeight);
        outState.putInt(KEY_WIDTH, mWidth);
        outState.putFloat(KEY_DIM, mDimAmount);
        outState.putBoolean(KEY_CANCEL_OUTSIDE, mIsCancelOutside);
        outState.putBoolean(KEY_CENTER, mCenter);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void bindView(View v) {
        if (mViewListener != null) {
            mViewListener.bindView(v);
        }
    }

    @Override
    public int getLayoutRes() {
        return mLayoutRes;
    }

    public CustomDialog setLayoutRes(@LayoutRes int layoutRes) {
        mLayoutRes = layoutRes;
        return this;
    }

    public CustomDialog setFragmentManager(FragmentManager manager) {
        mFragmentManager = manager;
        return this;
    }

    public CustomDialog setViewListener(ViewListener listener) {
        mViewListener = listener;
        return this;
    }

    public CustomDialog setTag(String tag) {
        mTag = tag;
        return this;
    }

    @Override
    public float getDimAmount() {
        return mDimAmount;
    }

    public CustomDialog setDimAmount(float dim) {
        mDimAmount = dim;
        return this;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    public CustomDialog setHeight(int heightPx) {
        mHeight = heightPx;
        return this;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    public CustomDialog setWidth(int widthPx) {
        mWidth = widthPx;
        return this;
    }

    @Override
    public boolean isCenter() {
        return mCenter;
    }

    public CustomDialog isCenter(boolean center) {
        mCenter = center;
        return this;
    }

    @Override
    public boolean getCancelOutside() {
        return mIsCancelOutside;
    }

    public CustomDialog setCancelOutside(boolean cancel) {
        mIsCancelOutside = cancel;
        return this;
    }

    @Override
    public String getFragmentTag() {
        return mTag;
    }

    public CustomDialog show() {
        show(mFragmentManager);
        return this;
    }

    public interface ViewListener {
        void bindView(View v);
    }
}
