package com.miyuan.adas;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * 界面由Fragment创建的Page
 *
 * @author guomin
 */
public abstract class FragmentPage extends BasePage {

    private PageFragment fragment;

    private FragmentActivity activity;

    public FragmentPage(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    final protected void doInit() {
        Log.e("page","doInit="+this.getClass().getName());
        fragment = new PageFragment();
        fragment.setPage(this);
    }

    @Override
    protected void doShow() {
        this.replaceAndCommit(fragment, Constants.BACK_STACK_TAG);
    }

    @Override
    protected Context getContext() {
        return activity;
    }

    @Override
    protected LayoutInflater getInflater() {
        return activity.getLayoutInflater();
    }

    /**
     * 替换一个Fragment并提交 (自动处理是否记录上一页进入后退栈)
     *
     * @param fragment
     */
    public void replaceAndCommit(BaseFragment fragment) {
        this.replaceAndCommit(getContainerViewId(), fragment);
    }

    public void replaceAndCommit(int containerViewId, BaseFragment fragment) {
        this.replaceAndCommit(containerViewId, fragment, null);
    }

    public void replaceAndCommit(BaseFragment fragment, String tag) {
        this.replaceAndCommit(getContainerViewId(), fragment, tag);
    }

    public void replaceAndCommit(int containerViewId, BaseFragment fragment, String tag) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(containerViewId, fragment);
        boolean checkBackStack = checkBackStack();
        if (checkBackStack) {
            transaction.addToBackStack(tag);
        }
        setId(transaction.commitAllowingStateLoss());
    }

    /**
     * 添加一个Fragment并提交 (自动处理是否记录上一页进入后退栈)
     *
     * @param fragment
     */
    public void addAndCommit(Fragment fragment) {
        this.addAndCommit(getContainerViewId(), fragment);
    }

    public void addAndCommit(int containerViewId, Fragment fragment) {
        this.addAndCommit(containerViewId, fragment, null);
    }

    public void addAndCommit(Fragment fragment, String tag) {
        this.addAndCommit(getContainerViewId(), fragment, tag);
    }


    public void addAndCommit(int containerViewId, Fragment fragment, String tag) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.add(containerViewId, fragment);
        transaction.addToBackStack(tag);
        setId(transaction.commitAllowingStateLoss());
    }

    /**
     * 移除当前Fragment
     */
    void remove() {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }

    protected abstract int getContainerViewId();

    @Override
    ViewGroup getPageContainer() {
        ViewGroup containerView = (ViewGroup) activity.findViewById(getContainerViewId());
        return containerView;
    }

    /**
     * 每当切换Fragement时通过此方法, 检查是否进行addToBackStack()
     *
     * @return
     */
    private boolean checkBackStack() {
        BasePage prev = getPrev();
        if (null == prev) {
            return false;
        }
        return prev.isToHistory();
    }

    @Override
    public void back(int targetId, boolean flag) {
        if (-2 == targetId) { // 跳到最后一个Page页面
            activity.getSupportFragmentManager().popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (-1 == targetId) { // -1是该Page的前一个Page不入栈
            activity.getSupportFragmentManager().popBackStack();
        } else {
            if (flag) {
                activity.getSupportFragmentManager().popBackStack(targetId, 0);
            } else {
                // 该Page的前一个Page没有入栈,后一个Page入栈了,pop的目的是为了移除当前Page的Fragment(直接无法移除,因为它已经被加栈)
                activity.getSupportFragmentManager().popBackStack(targetId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
    }
}
