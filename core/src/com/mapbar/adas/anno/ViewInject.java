package com.mapbar.adas.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewInject {

    /**
     * content view layout id<br>
     * 自行创建布局时勿填写此项
     *
     * @return
     */
    int value() default 0;
}
