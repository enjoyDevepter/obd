package com.mapbar.adas.log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author baimi
 */
public class TestConfigListener implements Listener.GenericListener<IEConfig.ConfigEventInfo> {

    @Override
    public void onEvent(IEConfig.ConfigEventInfo eventInfo) {
        TestHelper helper = TestHelper.getInstance();
        // 日志
        final JSONObject internal = eventInfo.getInternal();
        if (internal != null) {
            // 内部配置
            helper.setConfigJson(internal);
        }
        final JSONObject external = eventInfo.getExternal();
        if (external != null) {
            // 避免覆盖测试人
            if (internal != null) {
                try {
                    external.putOpt(IEConfig.TESTER, internal.optString(IEConfig.TESTER));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // 外部配置
            helper.setConfigJson(external);
        }

        helper.initReplaceUri();

//        helper.showTip(GlobalUtil.getContext());
    }

}
