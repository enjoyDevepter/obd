package com.miyuan.obd.log;

import org.json.JSONObject;

/**
 * internal and external config
 *
 * @author baimi
 */
public abstract class IEConfig {

    /**
     * 没有测试者(关闭所有的测试)
     */
    public static final String TESTER_NO = "no";
    /**
     * 测试者
     */
    public static final String TESTER = "tester";

    private final LogConfigListener logConfigListener = new LogConfigListener();
    private final TestConfigListener testConfigListener = new TestConfigListener();
    /**
     * 是否已初始化内部配置
     */
    private boolean init;
    private JSONObject internalConfigJson;
    private JSONObject externalConfigJson;
    private WeakGenericListeners<ConfigEventInfo> listeners = new WeakGenericListeners<ConfigEventInfo>();

    /**
     * 强制初始化<br>
     * 用于已经初始化之后强行再次初始化，例如：SD卡外部配置文件更换而不希望重新启动app<br>
     * 不过此功能没有经过测试，应该不安全也不可靠，最好先别使用
     */
    public void forceInit() {
        init = false;
        init();
    }

    public void init() {
        if (!init) {
            System.out.println("log: init");
            init = true;
            addListener(testConfigListener);
            addListener(logConfigListener);
            try {
                internalConfigJson = loadInternalConfigJsonString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                externalConfigJson = loadExternalConfigJsonString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            listeners.conveyEvent(new ConfigEventInfo(internalConfigJson, externalConfigJson));
            initClientConfig();
        }
    }

    /**
     * 读取内部配置
     *
     * @return
     */
    protected abstract JSONObject loadInternalConfigJsonString() throws Exception;

    /**
     * 读取外部配置
     *
     * @return
     */
    protected abstract JSONObject loadExternalConfigJsonString() throws Exception;

    protected abstract void initClientConfig();

    public void addListener(Listener.GenericListener<ConfigEventInfo> listener) {
        listeners.add(listener);
    }

    public static class ConfigEventInfo extends BaseEventInfo {

        private JSONObject internal;

        private JSONObject external;

        public ConfigEventInfo(JSONObject internal, JSONObject external) {
            this.internal = internal;
            this.external = external;
        }

        public JSONObject getInternal() {
            return internal;
        }

        public void setInternal(JSONObject internal) {
            this.internal = internal;
        }

        public JSONObject getExternal() {
            return external;
        }

        public void setExternal(JSONObject external) {
            this.external = external;
        }

    }

}
