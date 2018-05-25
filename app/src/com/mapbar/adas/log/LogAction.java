package com.mapbar.adas.log;

/**
 * 日志动作
 */
public class LogAction {

    /**
     * 是否可以打印
     */
    private boolean loggable = true;
    /**
     * 是否打印堆栈（调用链）
     */
    private Boolean stack;
    /**
     * 是否强行要求都打印包名或都不打印包名
     */
    private Boolean packageName;

    /**
     * @return the {@link #loggable}
     */
    public boolean isLoggable() {
        return loggable;
    }

    /**
     * @param loggable the {@link #loggable} to set
     */
    public void setLoggable(boolean loggable) {
        this.loggable = loggable;
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

    /**
     * @return the {@link #packageName}
     */
    public Boolean getPackageName() {
        return packageName;
    }

    /**
     * @param simpleClassName the {@link #packageName} to set
     */
    public void setPackageName(Boolean simpleClassName) {
        packageName = simpleClassName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogAction [loggable=" + loggable + ", stack=" + stack + ", packageName=" + packageName + "]";
    }

}
