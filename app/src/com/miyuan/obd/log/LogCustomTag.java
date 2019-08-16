package com.miyuan.obd.log;

import java.util.HashMap;

public class LogCustomTag implements LogTagInterface {

    private static HashMap<String, LogCustomTag> map = new HashMap<String, LogCustomTag>();

    private String tagName = "custom";

    private LogCustomTag(String s) {
        tagName = s;
    }

    /**
     * 创建或得到自定义日志标签
     *
     * @param tagName
     * @return
     */
    public synchronized static LogCustomTag createOrGetLogCustomTag(String tagName) {
        LogCustomTag logCustomTag = LogCustomTag.map.get(tagName);
        if (null == logCustomTag) {
            logCustomTag = new LogCustomTag(tagName);
        }
        return logCustomTag;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

}
