package com.mapbar.adas.log;

/**
 *
 */
public enum LogTag implements LogTagInterface {
    /**
     * 测试
     */
    TEMP,
    UI,
    DETECT,
    GLOBAL,
    STORAGE_DEVICE,
    /**
     * 所有（研发请勿使用）
     */
    @Deprecated
    ALL;

    @Override
    public String getTagName() {
        return name();
    }

}
