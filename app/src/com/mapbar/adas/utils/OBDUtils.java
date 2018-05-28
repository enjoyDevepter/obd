package com.mapbar.adas.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by guomin on 2018/5/28.
 */

public class OBDUtils {

    public static int getDimens(Context context, int id) {
        return (int) getResources(context).getDimension(id);
    }

    /**
     * 获得资源
     */
    public static Resources getResources(Context context) {
        return context.getResources();
    }


}
