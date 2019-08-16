package com.miyuan.adas;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;

import java.lang.reflect.Field;

/**
 * @author guomin 界面基类
 */
public abstract class BasePage implements ILifeCycleListener {

    public static final int FLAG_SINGLE_TASK = 1;

    private static final int INITING = 0;
    private static final int CALLED_ON_CREATE_VIEW = 1;
    private static final int CALLED_ON_START = 2;
    private static final int CALLED_ON_RESUME = 3;
    private static final int CALLED_ON_PAUSE = 4;

    private static final int CALLED_ON_STOP = 5;
    private static final int CALLED_ON_DESTROY = 6;
    private int mCurrentState = INITING;

    private int contentViewId = 0;

    /**
     * 页面id
     * 可能的值是: 1.FragTransaction提交之后生成的id
     */
    private int id;
    /**
     * 是否透明
     */
    private boolean transparent;
    /**
     * 确定本页面是否加入历史栈
     */
    private boolean toHistory = true;
    /**
     * 后退时是否跳过该页面
     */
    private boolean isSkip;

    private int flag = 0;

    private BasePage prev;

    private Bundle date;


    public BasePage() {
        PageSetting pageSetting = this.getClass().getAnnotation(PageSetting.class);
        if (pageSetting != null) {
            setTransparent(pageSetting.transparent());
            setContentViewId(pageSetting.contentViewId());
            setToHistory(pageSetting.toHistory());
            setFlag(pageSetting.flag());
        }
    }

    public void init() {
        doInit();
    }

    /**
     * 初始化
     */
    protected abstract void doInit();

    public void show() {
        doShow();
    }

    /**
     * 界面显示
     */
    protected abstract void doShow();

    protected abstract Context getContext();

    protected abstract LayoutInflater getInflater();

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    abstract ViewGroup getPageContainer();

    @Override
    public View onCreateView() {
        mCurrentState = CALLED_ON_CREATE_VIEW;
        View contentView = getInflater().inflate(getContentViewId(), null);
        if (contentView != null) {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject == null) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(this, contentView.findViewById(viewInject.value()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return contentView;
    }

    void onDestroyView() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.getLayoutDirection() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {

        } else if (newConfig.getLayoutDirection() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {

        }
    }


    @Override
    public void onStart() {
        mCurrentState = CALLED_ON_START;
    }

    @Override
    public void onResume() {
        mCurrentState = CALLED_ON_RESUME;
    }

    @Override
    public void onPause() {
        mCurrentState = CALLED_ON_PAUSE;
    }

    @Override
    public void onStop() {
        mCurrentState = CALLED_ON_STOP;
    }

    @Override
    public void onDestroy() {
    }

    /**
     * Page后退时的处理
     */
    public void back(int targetId, boolean flag) {
    }

    /**
     * 返回监听
     *
     * @return 返回true表示当前Page自己监听返回事件, 否则走默认的back
     */
    public boolean onBackPressed() {
        return false;
    }

    public boolean isTransparent() {
        return transparent;
    }

    /**
     * @param transparent the {@link #transparent} to set
     */
    void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean isToHistory() {
        return toHistory;
    }

    public void setToHistory(boolean toHistory) {
        this.toHistory = toHistory;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean skip) {
        isSkip = skip;
    }

    public BasePage getPrev() {
        return prev;
    }

    public void setPrev(BasePage prev) {
        this.prev = prev;
    }

    int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getContentViewId() {
        return contentViewId;
    }

    public void setContentViewId(int contentViewId) {
        this.contentViewId = contentViewId;
    }

    public Bundle getDate() {
        return date;
    }

    public void setDate(Bundle date) {
        this.date = date;
    }
}
