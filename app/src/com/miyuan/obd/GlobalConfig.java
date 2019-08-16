package com.miyuan.obd;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.log.IEConfig;
import com.miyuan.obd.log.MapbarStorageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author guomin
 */
public class GlobalConfig extends IEConfig {

    private static final String LOG_FILE_NAME = "new_obd.log";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    /**
     * 测试者
     */
    private String tester = IEConfig.TESTER_NO;

    private Process mProcess;

    @Override
    protected JSONObject loadInternalConfigJsonString() throws JSONException, IOException {
        final String json = GlobalUtil.getFromAssets(GlobalUtil.getContext(), "config.json");
        System.out.println("log: loadInternalConfigJsonString = " + json);
        JSONObject obj = new JSONObject(json);
        tester = obj.getString(IEConfig.TESTER);
        return obj;
    }

    @Override
    protected JSONObject loadExternalConfigJsonString() throws JSONException {
        return null;
    }

    @Override
    protected void initClientConfig() {
        try {
            String dirPath = MapbarStorageUtil.getCurrentValidMapbarPath();
            final File dir = new File(dirPath);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            if (mProcess != null) {
                mProcess.destroy();
            }
            File file = new File(dir, LOG_FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
            mProcess = Runtime.getRuntime().exec("logcat -v long -f " + file.getPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
