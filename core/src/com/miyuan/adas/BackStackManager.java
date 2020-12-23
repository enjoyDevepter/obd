package com.miyuan.adas;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guomin 后退栈管理器 (内存和磁盘一起, 暂不支持磁盘)
 */
public class BackStackManager {

    /**
     * 当前界面
     */
    private BasePage current;
    /**
     * 历史界面
     */
    private List<BasePage> historyList = new ArrayList<>();

    /**
     * 禁止构造
     */
    private BackStackManager() {
    }

    /**
     * 获得单例
     */
    public static BackStackManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 压栈
     */
    public void push(BasePage page) {
        historyList.add(page);
    }

    /**
     * 弹栈
     *
     * @return
     */
    public int pop() {
        BasePage prev = getPrev();

        if (!prev.equals(current.getPrev())) {
            ((FragmentPage) current).remove();
        }

        BasePage backPage = current;

        current = historyList.remove(size() - 1);

        if (current.isSkip()) {
            prev = getPrev();
            if (!current.getPrev().equals(prev)) {
                if (backPage.isToHistory()) {
                    current.back(backPage.getId(), false);
                } else {
                    current.back(-1, true);
                }
                ((FragmentPage) current).remove();
            }

            if (prev.isSkip()) {
                return pop();
            } else {
                backPage = current;
                current = historyList.remove(size() - 1);
            }
        }

        if (isLast()) { // current是最后一个界面
            return -2;
        }
        return current.getId();
    }

    /**
     * 获取栈中的上一个界面<br>
     * 如果页面是不入历史栈，就找不到
     *
     * @return
     */
    public BasePage getPrev() {
        return isLast() ? null : historyList.get(size() - 1);
    }

    /**
     * 是否当前 page
     *
     * @param page
     * @return
     */
    public boolean isCurrentPage(BasePage page) {
        return getCurrent() == page;
    }

    /**
     * 是否只剩最后一个
     *
     * @return
     */
    public boolean isLast() {
        // 如果内存是最后一个, 再看磁盘是否最后一个
        return isMLast() && isDLast();
    }

    /**
     * 内存是否只剩最后一个
     *
     * @return
     */
    public boolean isMLast() {
        return size() < 1;
    }

    /**
     * 磁盘是否只剩最后一个
     *
     * @return
     */
    public boolean isDLast() {
        // FIXME 未做
        return true;
    }

    /**
     * 栈项个数
     *
     * @return
     */
    public int size() {
        return historyList.size();
    }

    /**
     * 判断上一个界面是否应该加入历史, 是则加入
     */
    public void tryPushPrev() {
        if ((current != null && current.isToHistory())) {
            push(current);
        }
    }

    /**
     * 获得当前界面
     *
     * @return
     */
    public BasePage getCurrent() {
        return current;
    }

    /**
     * 设置当前界面
     *
     * @param current
     */
    public void setCurrent(BasePage current) {
        tryPushPrev();
        this.current = current;
    }


    /**
     * 为了 singleTask 机制<br>
     * 1.跳过当前页面到目标页面之间的所有页面（不包括目标页面）<br>
     * 2.并不会一直往前找，因为 singleTask 与类相关不会在运行时变化，所以找到一个已经跳过的，那前面的肯定也已经跳过<br>
     * 3.跳过的页面不算作是目标页面<br>
     * 4.找到目标页面才跳过之间的页面，否则放弃
     *
     * @param clazz
     * @return
     */
    public BasePage findPageAndSkipBetweenPages(@NonNull Class<? extends BasePage> clazz) {
        BasePage targetPage = null;
        List<BasePage> tempPageList = new ArrayList<>();
        for (int i = size() - 1; i >= 0; i--) {
            BasePage historyPage = historyList.get(i);
            if (clazz.isInstance(historyPage)) {
                if (!historyPage.isSkip()) {
                    targetPage = historyPage;
                }
                break;
            }
            tempPageList.add(historyPage);
        }

        if (null != targetPage) {
            for (BasePage page : tempPageList) {
                page.setSkip(true);
            }
        }
        return targetPage;
    }

    public boolean contains(BasePage page) {
        if (!historyList.isEmpty()) {
            return historyList.contains(page);
        }
        return false;
    }

    /**
     * 单例持有器
     */
    private static final class InstanceHolder {
        private static final BackStackManager INSTANCE = new BackStackManager();
    }

}
