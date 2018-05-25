package com.mapbar.adas.log;

/**
 * 日志规则
 */
public class LogRule {

    /**
     * 标签
     */
    @SuppressWarnings("deprecation")
    private LogTagInterface tag = LogTag.ALL;
    /**
     * 级别
     */
    private Integer level;
    /**
     * 包名
     */
    private String packageName;
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * （代码中）是否打印堆栈（调用链）
     */
    private Boolean stack;

    /**
     * @return the {@link #tag}
     */
    public LogTagInterface getTag() {
        return tag;
    }

    /**
     * @param tag the {@link #tag} to set
     */
    public void setTag(LogTagInterface tag) {
        this.tag = tag;
    }

    /**
     * @return the {@link #level}
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * @param level the {@link #level} to set
     */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * @return the {@link #packageName}
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName the {@link #packageName} to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @return the {@link #className}
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the {@link #className} to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the {@link #methodName}
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the {@link #methodName} to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return the {@link #stack}
     */
    public Boolean getStack() {
        return stack;
    }

    /**
     * @param stack the {@link #stack} to set
     */
    public void setStack(Boolean stack) {
        this.stack = stack;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LogRule other = (LogRule) obj;
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogRule [tag=" + tag + ", level=" + level + ", packageName=" + packageName + ", className=" + className + ", methodName=" + methodName + ", stack=" + stack + "]";
    }

}
