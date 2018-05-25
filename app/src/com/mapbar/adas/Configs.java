package com.mapbar.adas;

import com.wedrive.welink.adas.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shisk on 2017/7/21.
 */

public class Configs {
    //图片的宽高
    public static int detectWidth;
    public static int detectHeight;
    //当前屏幕的宽高
    public static int width;
    public static int height;

    public static List<String> supportedListSizes = new ArrayList<>();

    public static boolean DEBUG = BuildConfig.DEBUG;

    public static boolean IS_SHOW_DETECTER_AREA = false;
}
