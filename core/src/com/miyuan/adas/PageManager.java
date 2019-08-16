package com.miyuan.adas;

import android.app.Activity;
import android.os.Process;
import android.support.annotation.NonNull;


/**
 * @author guomin
 */
public class PageManager {

    private static BackStackManager backStackManager = BackStackManager.getInstance();

    private PageManager() {
    }

    public static void go(@NonNull BasePage page) {

        switch (page.getFlag()) {
            case BasePage.FLAG_SINGLE_TASK:// singleTask机制
                // 的Page,打开页面时是将该页面之上所有Page都清除
                BasePage targetPage = backStackManager.findPageAndSkipBetweenPages(page.getClass());
                if (null != targetPage) {
                    back();
                    return;
                }
                break;
        }

        BasePage prev = backStackManager.getCurrent();

        page.setPrev(prev);

        // 设置新的当前Page
        backStackManager.setCurrent(page);

        page.init();

        page.show();
    }

    public static void back() {
        BasePage curPage = backStackManager.getCurrent();
        if (!backStackManager.isLast()) { // historyList中元素数
            int id = backStackManager.pop();
            curPage.back(id, true);
        } else {
            System.gc();
            System.exit(0);
        }
    }

    public static void clearHistoryAndGo(@NonNull BasePage page) {
        BasePage curPage = backStackManager.getCurrent();
        while (!backStackManager.isLast()) {
            int id = backStackManager.pop();
            curPage.back(id, true);
        }
        backStackManager.setCurrent(page);
        page.init();
        page.show();
    }

    /**
     * 结束指定的Activity
     */
    public static void finishActivity(Activity activity) {
        if (null != activity) {
            activity.finish();
            Process.killProcess(Process.myPid());
        }
    }
}
