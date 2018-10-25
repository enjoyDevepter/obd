package com.mapbar.adas.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PageSetting {
    /**
     * 是否透明（默认值：false）
     * <br/>
     * true: 透明效果(可见底层地图)
     * <br/>
     * false: 不透明
     */
    boolean transparent() default false;

    /**
     * 是否加入历史或后退栈(默认值：true)
     * <br/>
     * true: 加入后退栈（点击返回按钮后可返回到改Page）
     * <br/>
     * false: 不加入后退栈
     */
    boolean toHistory() default true;


    /**
     * 页面跳转逻辑标记
     *
     * @see com.mapbar.adas.BasePage#FLAG_SINGLE_TASK
     */
    int flag() default 0;

    /**
     * 布局ID
     *
     * @return
     */
    int contentViewId() default 0;


    int contentViewLandId() default 0;
}