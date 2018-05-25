package com.mapbar.adas.log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * log rule and action
 */

/**
 * @author guomin
 */
public class LogRA {

    private final static String JSON_PARAMETER_NAME_RULE = "rule";
    private final static String JSON_PARAMETER_NAME_ACTION = "action";
    private final static String JSON_PARAMETER_NAME_TAG = "tag";
    private final static String JSON_PARAMETER_NAME_LEVEL = "level";
    private final static String JSON_PARAMETER_NAME_PACKAGE = "package";
    private final static String JSON_PARAMETER_NAME_CLASS = "class";
    private final static String JSON_PARAMETER_NAME_METHOD = "method";
    private final static String JSON_PARAMETER_NAME_STACK = "stack";
    private final static String JSON_PARAMETER_NAME_LOGGABLE = "loggable";

    private final static String JSON_PARAMETER_VALUE_ALL = "all";
    private final static String JSON_PARAMETER_VALUE_VERBOSE = "v";
    private final static String JSON_PARAMETER_VALUE_DEBUG = "d";
    private final static String JSON_PARAMETER_VALUE_INFO = "i";
    private final static String JSON_PARAMETER_VALUE_WARN = "w";
    private final static String JSON_PARAMETER_VALUE_ERROR = "e";
    private final static String JSON_PARAMETER_VALUE_INHERITANCE = "inheritance";

    private LogRule rule;
    private LogAction action;

    public static LogRA jsonToLogRA(JSONObject json) throws JSONException {
        LogRA logRA = new LogRA();
        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_RULE)) {
            logRA.setRule(LogRA.jsonToLogRule(json.getJSONObject(LogRA.JSON_PARAMETER_NAME_RULE)));
        } else {// 没定义的时候全部按默认的来
            logRA.setRule(new LogRule());
        }
        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_ACTION)) {
            logRA.setAction(LogRA.jsonToLogAction(json.getJSONObject(LogRA.JSON_PARAMETER_NAME_ACTION)));
        } else {// 没定义的时候全部按默认的来
            logRA.setAction(new LogAction());
        }
        return logRA;
    }

    public static LogRule jsonToLogRule(JSONObject json) throws JSONException {
        LogRule logRule = new LogRule();

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_TAG)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_TAG);
            if (!LogRA.JSON_PARAMETER_VALUE_ALL.equals(value)) {
                try {
                    logRule.setTag(LogTag.valueOf(value));
                } catch (IllegalArgumentException e) {
                    logRule.setTag(LogCustomTag.createOrGetLogCustomTag(value));
                }
            }
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_LEVEL)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_LEVEL);
            if (LogRA.JSON_PARAMETER_VALUE_ALL.equals(value)) {
            } else if (LogRA.JSON_PARAMETER_VALUE_VERBOSE.equals(value)) {
                logRule.setLevel(Log.VERBOSE);
            } else if (LogRA.JSON_PARAMETER_VALUE_DEBUG.equals(value)) {
                logRule.setLevel(Log.DEBUG);
            } else if (LogRA.JSON_PARAMETER_VALUE_INFO.equals(value)) {
                logRule.setLevel(Log.INFO);
            } else if (LogRA.JSON_PARAMETER_VALUE_WARN.equals(value)) {
                logRule.setLevel(Log.WARN);
            } else if (LogRA.JSON_PARAMETER_VALUE_ERROR.equals(value)) {
                logRule.setLevel(Log.ERROR);
            }
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_PACKAGE)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_PACKAGE);
            if (!LogRA.JSON_PARAMETER_VALUE_ALL.equals(value)) {
                logRule.setPackageName(value);
            }
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_CLASS)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_CLASS);
            if (!LogRA.JSON_PARAMETER_VALUE_ALL.equals(value)) {
                logRule.setClassName(value);
            }
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_METHOD)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_METHOD);
            if (!LogRA.JSON_PARAMETER_VALUE_ALL.equals(value)) {
                logRule.setMethodName(value);
            }
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_STACK)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_STACK);
            if (!LogRA.JSON_PARAMETER_VALUE_ALL.equals(value)) {
                logRule.setStack(Boolean.valueOf(value));
            }
        }

        return logRule;
    }

    public static LogAction jsonToLogAction(JSONObject json) throws JSONException {
        LogAction logAction = new LogAction();

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_LOGGABLE)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_LOGGABLE);
            logAction.setLoggable(Boolean.valueOf(value));
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_STACK)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_STACK);
            if (!LogRA.JSON_PARAMETER_VALUE_INHERITANCE.equals(value)) {
                logAction.setStack(Boolean.valueOf(value));
            }
        }

        if (!json.isNull(LogRA.JSON_PARAMETER_NAME_PACKAGE)) {
            String value = json.getString(LogRA.JSON_PARAMETER_NAME_PACKAGE);
            if (!LogRA.JSON_PARAMETER_VALUE_INHERITANCE.equals(value)) {
                logAction.setPackageName(Boolean.valueOf(value));
            }
        }

        return logAction;
    }

    public boolean check(int level, LogTagInterface tag, boolean stack) {
        Integer rLevel = rule.getLevel();
        LogTagInterface rTag = rule.getTag();
        Boolean rStack = rule.getStack();
        return ((null == rLevel) || rLevel.equals(level)) && //
                ((null == rTag) || tag.equals(rTag)) && //
                ((null == rStack) || rStack.equals(stack));
    }

    public boolean check(String packageName, String className, String methodName) {
        String rPackageName = rule.getPackageName();
        String rClassName = rule.getClassName();
        String rMethodName = rule.getMethodName();
        return ((null == rPackageName) || rPackageName.equals(packageName)) && //
                ((null == rClassName) || rClassName.equals(className)) && //
                ((null == rMethodName) || rMethodName.equals(methodName));
    }

    /**
     * @return the {@link #rule}
     */
    public LogRule getRule() {
        return rule;
    }

    /**
     * @param rule the {@link #rule} to set
     */
    public void setRule(LogRule rule) {
        this.rule = rule;
    }

    /**
     * @return the {@link #action}
     */
    public LogAction getAction() {
        return action;
    }

    /**
     * @param action the {@link #action} to set
     */
    public void setAction(LogAction action) {
        this.action = action;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((rule == null) ? 0 : rule.hashCode());
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
        LogRA other = (LogRA) obj;
        if (rule == null) {
            if (other.rule != null) {
                return false;
            }
        } else if (!rule.equals(other.rule)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogRA [rule=" + rule + ", action=" + action + "]";
    }

}
