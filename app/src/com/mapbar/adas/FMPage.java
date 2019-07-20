package com.mapbar.adas;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.gyf.barlibrary.ImmersionBar;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.FMStatus;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import java.util.ArrayList;
import java.util.List;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.FM_GUID;


@PageSetting(contentViewId = R.layout.fm_layout, toHistory = false)
public class FMPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    List<String> hundredItems = new ArrayList<>();
    List<String> tenItems = new ArrayList<>();
    List<String> bitItems = new ArrayList<>();
    List<String> centItems = new ArrayList<>();
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.mode)
    private ViewGroup modeVG;
    @ViewInject(R.id.hundred)
    private WheelView hundredWV;
    @ViewInject(R.id.ten)
    private WheelView tenWV;
    @ViewInject(R.id.bit)
    private WheelView bitWV;
    @ViewInject(R.id.cent)
    private WheelView centWV;
    @ViewInject(R.id.confirm)
    private View confirmV;
    private FMStatus fmStatus;


    private CustomDialog dialog;


    @Override
    public void onResume() {
        super.onResume();
        title.setText("FM设置");
        reportV.setVisibility(View.GONE);
        back.setOnClickListener(this);
        modeVG.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏


        hundredItems.add("0");
        hundredItems.add("1");
        hundredWV.setCyclic(false);
        hundredWV.setCurrentItem(0);
        hundredWV.setAdapter(new ArrayWheelAdapter(hundredItems));
        hundredWV.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                tenItems.clear();
                if (index == 0) {
                    tenItems.add("0");
                    tenItems.add("8");
                    tenItems.add("9");
                } else {
                    tenItems.add("0");
                    tenWV.setCurrentItem(0);
                    bitItems.clear();
                    bitItems.add("0");
                    bitItems.add("1");
                    bitItems.add("2");
                    bitItems.add("3");
                    bitItems.add("4");
                    bitItems.add("5");
                    bitItems.add("6");
                    bitItems.add("7");
                    bitItems.add("8");
                    bitWV.setCurrentItem(0);
                }

                if (centItems.size() == 1) {
                    centItems.clear();
                    centItems.add("0");
                    centItems.add("1");
                    centItems.add("2");
                    centItems.add("3");
                    centItems.add("4");
                    centItems.add("5");
                    centItems.add("6");
                    centItems.add("7");
                    centItems.add("8");
                    centItems.add("9");
                    centWV.invalidate();
                }
                tenWV.invalidate();
                bitWV.invalidate();
            }
        });

        tenItems.add("0");
        tenItems.add("8");
        tenItems.add("9");
        tenWV.setCyclic(false);
        tenWV.setCurrentItem(1);
        tenWV.setAdapter(new ArrayWheelAdapter(tenItems));
        tenWV.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                hundredItems.clear();
                bitItems.clear();
                if (index == 0) { // 0
                    hundredItems.add("0");
                    hundredItems.add("1");

                    bitItems.add("0");
                    bitItems.add("1");
                    bitItems.add("2");
                    bitItems.add("3");
                    bitItems.add("4");
                    bitItems.add("5");
                    bitItems.add("6");
                    bitItems.add("7");
                    bitItems.add("8");
                    bitWV.setCurrentItem(0);
                } else if (index == 1) { // 8
                    hundredItems.add("0");
                    bitItems.clear();
                    bitItems.add("0");
                    bitItems.add("1");
                    bitItems.add("2");
                    bitItems.add("3");
                    bitItems.add("4");
                    bitItems.add("5");
                    bitItems.add("6");
                    bitItems.add("7");
                    bitWV.setCurrentItem(0);
                } else if (index == 2) { //9
                    hundredItems.add("0");
                    bitItems.add("0");
                    bitItems.add("1");
                    bitItems.add("2");
                    bitItems.add("3");
                    bitItems.add("4");
                    bitItems.add("5");
                    bitItems.add("6");
                    bitItems.add("7");
                    bitItems.add("8");
                    bitItems.add("9");
                    bitWV.setCurrentItem(0);
                }
                if (centItems.size() == 1) {
                    centItems.clear();
                    centItems.add("0");
                    centItems.add("1");
                    centItems.add("2");
                    centItems.add("3");
                    centItems.add("4");
                    centItems.add("5");
                    centItems.add("6");
                    centItems.add("7");
                    centItems.add("8");
                    centItems.add("9");
                    centWV.invalidate();
                }
                hundredWV.invalidate();
                bitWV.invalidate();
            }
        });

        bitItems.add("0");
        bitItems.add("1");
        bitItems.add("2");
        bitItems.add("3");
        bitItems.add("4");
        bitItems.add("5");
        bitItems.add("6");
        bitItems.add("7");
        bitItems.add("8");
        bitItems.add("9");
        bitWV.setCurrentItem(7);
        bitWV.setCyclic(false);
        bitWV.setAdapter(new ArrayWheelAdapter(bitItems));
        bitWV.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                centItems.clear();
                if ("1".equals(hundredItems.get(hundredWV.getCurrentItem())) && "0".equals(tenItems.get(tenWV.getCurrentItem())) && "8".equals(bitItems.get(bitWV.getCurrentItem()))) {
                    centItems.add("0");
                    centWV.setCurrentItem(0);
                } else {
                    centItems.add("0");
                    centItems.add("1");
                    centItems.add("2");
                    centItems.add("3");
                    centItems.add("4");
                    centItems.add("5");
                    centItems.add("6");
                    centItems.add("7");
                    centItems.add("8");
                    centItems.add("9");
                }
                centWV.invalidate();
            }
        });

        centItems.add("0");
        centItems.add("1");
        centItems.add("2");
        centItems.add("3");
        centItems.add("4");
        centItems.add("5");
        centItems.add("6");
        centItems.add("7");
        centItems.add("8");
        centItems.add("9");
        centWV.setCyclic(false);
        centWV.setCurrentItem(5);
        centWV.setAdapter(new ArrayWheelAdapter(centItems));
        centWV.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
            }
        });

        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getFMParams(true, 0));

        if (!FM_GUID.get()) {
            showDailog();
        }
    }

    private void showDailog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View confirm = view.findViewById(R.id.confirm);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FM_GUID.set(true);
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_fm)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }


    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.mode:
                if (null != fmStatus) {
                    BlueManager.getInstance().send(ProtocolUtils.setFMParams(!modeVG.isSelected(), fmStatus.getRate()));
                }
                break;
            case R.id.confirm:
                if (null != fmStatus) {
                    int rate = Integer.valueOf(hundredItems.get(hundredWV.getCurrentItem())) * 1000 + Integer.valueOf(tenItems.get(tenWV.getCurrentItem())) * 100 + Integer.valueOf(bitItems.get(bitWV.getCurrentItem())) * 10 + Integer.valueOf(centItems.get(centWV.getCurrentItem()));
                    System.out.println("rate ===" + rate);
                    BlueManager.getInstance().send(ProtocolUtils.setFMParams(true, rate));
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.FM_PARAMS_INFO:
                fmStatus = (FMStatus) data;
                Log.d("fmStatus " + fmStatus);
                updateUI();
                break;
            default:
                break;
        }
    }

    private void updateUI() {
        if (null != fmStatus) {
            modeVG.setSelected(fmStatus.isEnable());
            TextView highModeFirstTV = (TextView) modeVG.getChildAt(0);
            TextView highModeSecondTV = (TextView) modeVG.getChildAt(1);

            if (modeVG.isSelected()) {
                highModeFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
                highModeFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
                highModeSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
                highModeSecondTV.setBackgroundColor(Color.TRANSPARENT);
            } else {
                highModeSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
                highModeSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
                highModeFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
                highModeFirstTV.setBackgroundColor(Color.TRANSPARENT);
            }

            confirmV.setVisibility(fmStatus.isEnable() ? View.VISIBLE : View.INVISIBLE);

            int rate = fmStatus.getRate();
            String rateStr = String.valueOf(rate);
            if (rate > 1000) {
                hundredWV.setCurrentItem(hundredItems.indexOf("1"));
                tenWV.setCurrentItem(tenItems.indexOf(rateStr.substring(1, 2)));
                bitWV.setCurrentItem(bitItems.indexOf(rateStr.substring(2, 3)));
                centWV.setCurrentItem(centItems.indexOf(rateStr.substring(3, 4)));
            } else if (rate >= 100) {
                hundredWV.setCurrentItem(hundredItems.indexOf("0"));
                tenWV.setCurrentItem(tenItems.indexOf(rateStr.substring(0, 1)));
                bitWV.setCurrentItem(bitItems.indexOf(rateStr.substring(1, 2)));
                centWV.setCurrentItem(centItems.indexOf(rateStr.substring(2, 3)));
            } else if (rate >= 10) {
                hundredWV.setCurrentItem(hundredItems.indexOf("0"));
                tenWV.setCurrentItem(tenItems.indexOf("0"));
                bitWV.setCurrentItem(bitItems.indexOf(rateStr.substring(0, 1)));
                centWV.setCurrentItem(centItems.indexOf(rateStr.substring(1, 2)));
            } else {
                hundredWV.setCurrentItem(hundredItems.indexOf("0"));
                tenWV.setCurrentItem(tenItems.indexOf("0"));
                bitWV.setCurrentItem(bitItems.indexOf("0"));
                centWV.setCurrentItem(centItems.indexOf(rateStr.substring(0, 1)));
            }
        }
    }
}
