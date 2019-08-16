package com.miyuan.adas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author guomin Fragment基类
 */
public abstract class BaseFragment extends Fragment {
    protected FragmentActivity activity;
    private ILifeCycleListener lifeCycleListener;
    private boolean isDetach;
    /**
     * 是否透明（暂时只支持预设，不支持后期修改，有需要请联系）
     */
    private boolean isTransparent = false;

    public BaseFragment() {
        super();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        System.out.println("=====onConfigurationChanged=====");
        super.onConfigurationChanged(newConfig);
        lifeCycleListener.onConfigurationChanged(newConfig);
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = (FragmentActivity) activity;
        super.onAttach(activity);
        System.out.println("=====onAttach=====");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("=====onCreate=====");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (isTransparent) {
            container.setBackgroundDrawable(null);
        } else {
            container.setBackgroundColor(0xfff2f2f2);
        }
        System.out.println("=====onCreateView=====");
        return realCreateView(inflater, container, false);
    }

    /**
     * 创建View的方法, 将其改到onCreate中, 避免fragment切换时再次createView
     */
    protected abstract View realCreateView(LayoutInflater inflater, ViewGroup container, boolean attachToRoot);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("=====onActivityCreated=====");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("=====onStart=====");
        lifeCycleListener.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("=====onResume=====");
        lifeCycleListener.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("=====onPause=====");
        lifeCycleListener.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("=====onStop=====");
        lifeCycleListener.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        System.out.println("=====onDestroyView=====");
        realDestroyView();
    }

    protected abstract void realDestroyView();

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("=====onDestroy=====");
        lifeCycleListener.onDestroy();
        lifeCycleListener = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("=====onDetach=====");
        setDetach(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("=====onSaveInstanceState=====");
    }

    public void registLifeCycleListener(ILifeCycleListener lifeCycleListener) {
        this.lifeCycleListener = lifeCycleListener;
    }

    public boolean getDetach() {
        return isDetach;
    }

    public void setDetach(boolean isDetach) {
        this.isDetach = isDetach;
    }

    /**
     * @param isTransparent the {@link #isTransparent} to set
     */
    public void setTransparent(boolean isTransparent) {
        this.isTransparent = isTransparent;
    }

    public interface IStartListener {
        void onStart();
    }

    public interface IPauseListener {
        void onPause();
    }

    public interface IResumeListener {
        void onResume();
    }

    public interface IStopListener {
        void onStop();
    }

    public interface IDestroyListener {
        void onDestroy();
    }

    public interface IConfigurationListener {
        void onConfigurationChanged(Configuration newConfig);
    }

    public interface ICreateView {
        View onCreateView();
    }
}
