package com.mapbar.adas;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * 布局工具类
 */
public class LayoutUtils {

    public static final int CENTER = 0;
    public static final int CENTER_VERTICAL = 2;
    public static final int CENTER_HORIZONTAL = 1;

    public static final int WIDTH = 0;
    public static final int HEIGHT = 1;

    /**
     * 横屏补充用 FLAG
     */
    public static final int ITEM_TYPE_LANDSCAPE_FLAG = 1;

    /**
     * 屏幕宽度所对应的dp个数
     */
    private static final float WIDTH_DP_COUNT = 360;
    private static final Object ORIENTATION_CHANGE_SYN = new Object();
    /**
     * 默认未指定方向
     */
    private static final int DEFAULT_ORIENTATION = Configuration.ORIENTATION_UNDEFINED;
    /**
     * 宽高
     */
    private static int[] wh = null;
    /**
     * 调整后的 dp 与 px 的比值
     */
    private static float adjustedDensity = 0;
    /**
     * 初始值为"未指定"
     */
    private static int mMainActivityOrientation = DEFAULT_ORIENTATION;

    /**
     * 通过本方法使得所有分辨率下均以屏幕宽度除以 360 个 dp 的方式来确定每个 dp 对应多少 px <br>
     * 也就是说 36dp 将含义将变为屏幕宽度的三百六十分之三十六，也就是十分之一<br>
     * UI 线程限定
     */
    public static void proportional() {

        float currentEnvironmentDensity = GlobalUtil.getResources().getDisplayMetrics().density;// 当前环境密度
        if (adjustedDensity == currentEnvironmentDensity) {// 如果已经正确
            return;
        }

        int width = getScreenWH()[WIDTH];

//        width = correctWidth(width,currentEnvironmentDensity);// 纠正宽度（未来觉得不合适时再考虑使用）

        changeDensity(width / WIDTH_DP_COUNT);
    }

    /**
     * 纠正宽度
     *
     * @param width
     * @param density
     * @return
     */
    private static int correctWidth(int width, float density) {
        int statusBarHeight = (int) (density * 48);// 状态栏高度（一般48dp）
        int possibleWidth = width + statusBarHeight;// 可能的高度
        switch (possibleWidth) {
            case 320:
            case 480:
            case 600:
            case 640:
            case 720:
            case 1080:
            case 1440:
                return possibleWidth;
        }
        return width;
    }

    /**
     * 更改 dp 与 px 的转换比值<br>
     * UI 线程限定
     *
     * @param density 目标密度
     */
    private static void changeDensity(float density) {

        DisplayMetrics displayMetrics = GlobalUtil.getResources().getDisplayMetrics();

        if (density == displayMetrics.density) {// 如果已经正确
            adjustedDensity = density;
            return;
        }

        float ratio = density / displayMetrics.density;// 计算变化比例
        displayMetrics.scaledDensity *= ratio;// 按变化比例修改
        displayMetrics.densityDpi *= ratio;// 按变化比例修改

        displayMetrics.density = density;// 直接修改

        adjustedDensity = density;// 记录调整后的 dp 与 px 的比值

    }


    /**
     * 转化为含横竖屏信息的 item type
     *
     * @param sourceItemType 原 item type
     * @param isLandscape    是否横屏
     * @return 含横竖屏信息的 item type
     */
    public static int landPortItemType(int sourceItemType, boolean isLandscape) {
        int landPortType = sourceItemType << 1;
        if (isLandscape) {
            landPortType |= ITEM_TYPE_LANDSCAPE_FLAG;
        } else {
            landPortType &= ~ITEM_TYPE_LANDSCAPE_FLAG;
        }
        return landPortType;
    }

    /**
     * 还原为原 item type
     *
     * @param landPortItemType 含横竖屏信息的 item type
     * @return 原 item type
     */
    public static int sourceItemType(int landPortItemType) {
        int sourceType = landPortItemType >> 1;
        return sourceType;
    }

    public static int dp2px(float dipValue) {
        // INFO 工具/dip转px
        final float scale = GlobalUtil.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue) {
        // INFO 工具/px转dip
        final float scale = GlobalUtil.getResources().getDisplayMetrics().density;

        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(float pxValue) {
        final float fontScale = GlobalUtil.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(float spValue) {
        final float fontScale = GlobalUtil.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 注意使用常量取值！
     *
     * @return 宽总是短的，高总是长的
     */
    public static synchronized int[] getScreenWH() {

        if (null != wh) {
            return wh;
        }

        wh = new int[2];

        WindowManager windowManager = GlobalUtil.getMainActivity().getWindowManager();

        Display display = windowManager.getDefaultDisplay();
        mergeWH(display.getWidth(), display.getHeight());

        // since SDK_INT = 1;
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mergeWH(metrics.widthPixels, metrics.heightPixels);

        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                // includes window decorations (statusbar bar/menu bar)
                int a = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                int b = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
                mergeWH(a, b);
            } catch (Exception e) {
            }
        }

        if (Build.VERSION.SDK_INT >= 17) {
            // includes window decorations (statusbar bar/menu bar)
            Point realSize = new Point();
            display.getRealSize(realSize);
            mergeWH(realSize.x, realSize.y);
        }

        return wh;
    }

    /**
     * 1、判断哪个是宽哪个是高<br>
     * 2、宽高分别只留下最大值
     *
     * @param a
     * @param b
     */
    private static void mergeWH(int a, int b) {
        if (a < b) {
            wh[WIDTH] = Math.max(wh[WIDTH], a);
            wh[HEIGHT] = Math.max(wh[HEIGHT], b);
        } else {
            wh[WIDTH] = Math.max(wh[WIDTH], b);
            wh[HEIGHT] = Math.max(wh[HEIGHT], a);
        }
    }

    /**
     * 给定父矩形和子矩形的宽高，自动将子矩形修改为居中区域
     *
     * @param centerType 1:水平居中 2:垂直居中 0:全部居中
     */
    public static Rect getCenter(Rect outer, Rect inner, int centerType) {
        return getCenter(outer.left, outer.top, outer.right, outer.bottom, inner, centerType);
    }

    /**
     * 给定父矩形和子矩形的宽高，自动将子矩形修改为居中区域
     *
     * @param centerType 1:水平居中 2:垂直居中 0:全部居中
     */
    public static Rect getCenter(int left, int top, int right, int bottom, Rect inner, int centerType) {
        int w = inner.width();
        int h = inner.height();
        switch (centerType) {
            case CENTER:
                inner.top = (bottom - top - h) / 2 + top;
                inner.bottom = inner.top + h;
            case CENTER_HORIZONTAL:
                inner.left = (right - left - w) / 2 + left;
                inner.right = inner.left + w;
                break;
            case CENTER_VERTICAL:
                inner.top = (bottom - top - h) / 2 + top;
                inner.bottom = inner.top + h;
                break;
        }
        return inner;
    }

    /**
     * 获取定义在 dimens 的尺寸
     *
     * @param id
     * @return 像素值
     */
    public static int getPxByDimens(int id) {
        return GlobalUtil.getResources().getDimensionPixelSize(id);
    }

    /**
     * 获取定义在 dimens 的尺寸
     *
     * @param res
     * @param id
     * @return 像素值
     */
    public static int getPxByDimens(Resources res, int id) {
        return res.getDimensionPixelSize(id);
    }

    /**
     * 获取定义在 color 的值
     *
     * @param id
     * @return 颜色值
     */
    public static int getColorById(int id) {
        return GlobalUtil.getResources().getColor(id);
    }


    public static int getScreenOrientation() {
        Display getOrient = GlobalUtil.getMainActivity().getWindowManager().getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() < getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    public static boolean contains(int[] array, int selectElement) {
        for (int i : array) {
            if (i == selectElement) {
                return true;
            }
        }
        return false;
    }

    /**
     * 绘制文字基线为中轴位置
     *
     * @return
     */
    public static float baseline(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
    }

}
