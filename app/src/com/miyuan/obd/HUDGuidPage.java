package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.view.View;

import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.obd.preferences.SettingPreferencesConfig;

@PageSetting(contentViewId = R.layout.hud_guid_layout, toHistory = false)
public class HUDGuidPage extends AppBasePage implements View.OnClickListener {
    @ViewInject(R.id.step1)
    View step1V;
    @ViewInject(R.id.step2)
    View step2V;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        step1V.setOnClickListener(this);
        step2V.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.step1:
                step1V.setVisibility(View.INVISIBLE);
                step2V.setVisibility(View.VISIBLE);
                break;
            case R.id.step2:
                SettingPreferencesConfig.HUD_GUID.set(true);
                // 页面跳转
                if (null != getDate()) {
                    switch (getDate().getInt("hudType")) {
                        case 0x02:
                            PageManager.go(new M2SettingPage());
                            break;
                        case 0x03:
                            PageManager.go(new M3SettingPage());
                            break;
                        case 0x04:
                            PageManager.go(new M4SettingPage());
                            break;
                        case 0x22:
                            PageManager.go(new F2SettingPage());
                            break;
                        case 0x23:
                            PageManager.go(new F3SettingPage());
                            break;
                        case 0x24:
                            PageManager.go(new F4SettingPage());
                            break;
                        case 0x25:
                            PageManager.go(new F5SettingPage());
                            break;
                        case 0x26:
                            PageManager.go(new F6SettingPage());
                            break;
                        case 0x43:
                            PageManager.go(new P3SettingPage());
                            break;
                        case 0x44:
                            PageManager.go(new P4SettingPage());
                            break;
                        case 0x45:
                            PageManager.go(new P5SettingPage());
                            break;
                        case 0x46:
                            PageManager.go(new P6SettingPage());
                            break;
                        case 0x47:
                            PageManager.go(new P7SettingPage());
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
    }
}
