package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.core.HexUtils;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.obd.utils.DBManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.miyuan.hamster.OBDEvent.COMMON_INFO;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_FIVE;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_FOUR;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_ONE;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_SEVEN;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_SEX;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_THREE;
import static com.miyuan.hamster.OBDEvent.PHYSICAL_STEP_TWO;

@PageSetting(contentViewId = R.layout.physical_layout)
public class PhysicalPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    DecimalFormat decimalFormat = new DecimalFormat("#.##");//构造方法的字符格式这里如果小数不足2位,会以0补足.
    String[] types = new String[]{"发动机控制系统", "点火控制系统", "供电控制系统", "润滑控制系统", "冷却控制系统", "燃油及空气系统", "排放控制系统"};
    boolean[] status = new boolean[]{true, true, true, true, true, true, true};
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.home)
    private View homeV;
    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.connect)
    private View connect;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    @ViewInject(R.id.content)
    private RecyclerView contentLV;
    private List<Physical> physicalList;
    private Map<Integer, Physicaltem> allList;
    private List<Physicaltem> normalList = new ArrayList<>();
    private List<Physicaltem> errorPhysicalList = new ArrayList<>();
    private NormalAdapter normalAdapter;

    private boolean checked;
    private boolean start;

    private AnimationDrawable animationDrawable;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BlueManager.getInstance().addBleCallBackListener(this);
        titleTV.setText("正在体检");
        back.setOnClickListener(this);
        homeV.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
        confirmV.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
        confirmV.setOnClickListener(checked ? this : null);
        if (!checked) {
            initPhysical();
            BlueManager.getInstance().send(ProtocolUtils.stopGetNewTirePressureStatus());
        }
        normalAdapter = new NormalAdapter(physicalList);
        contentLV.setLayoutManager(new LinearLayoutManager(getContext()));
        contentLV.setAdapter(normalAdapter);

        ImmersionBar.with(GlobalUtil.getMainActivity())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    private void initPhysical() {
        physicalList = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            Physical physical = new Physical();
            physical.setName(types[i]);
            physicalList.add(physical);
        }
        physicalList.get(0).setStatus("0");
        // getPhysical from bd
        allList = DBManager.getInstance().getAll();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checked) {
            connect.setBackgroundResource(R.drawable.physical_scan_bg);
            animationDrawable = (AnimationDrawable) connect.getBackground();
            animationDrawable.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                PhysicalResultPage page = new PhysicalResultPage();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("normal", (ArrayList<? extends Parcelable>) normalList);
                bundle.putParcelableArrayList("error", (ArrayList<? extends Parcelable>) errorPhysicalList);
                page.setDate(bundle);
                PageManager.go(page);
                break;
            case R.id.home:
                PageManager.go(new HomePage());
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case PHYSICAL_STEP_ONE:
                parasePhysicalResult((byte[]) data);
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(02));
                break;
            case PHYSICAL_STEP_TWO:
                parasePhysicalResult((byte[]) data);
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(03));
                break;
            case PHYSICAL_STEP_THREE:
                parasePhysicalResult((byte[]) data);
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(04));
                break;
            case PHYSICAL_STEP_FOUR:
                parasePhysicalResult((byte[]) data);
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(05));
                break;
            case PHYSICAL_STEP_FIVE:
                parasePhysicalResult((byte[]) data);
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(06));
                break;
            case PHYSICAL_STEP_SEX:
                parasePhysicalResult((byte[]) data);
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(07));
                break;
            case PHYSICAL_STEP_SEVEN:
                parasePhysicalResult((byte[]) data);
                confirmV.setVisibility(View.VISIBLE);
                confirmV.setOnClickListener(this);
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                checked = true;
                break;
            case COMMON_INFO:
                if (!checked && !start) {
                    start = true;
                    BlueManager.getInstance().send(ProtocolUtils.sendPhysical(01)); // 开始体检
                }
                break;
        }
    }

    private void parasePhysicalResult(byte[] result) {
        int index = (HexUtils.byteToInt(result[0]) - 1);
        int count = HexUtils.byteToInt(result[1]);
        Physical physical = physicalList.get(index);
        physical.setCount(count);
        if (types.length - 1 == index) {
            normalAdapter.notifyDataSetChanged();
        } else {
            normalAdapter.notifyItemRangeChanged(index, 2);
        }
        for (int i = 1; i <= count; i++) {
            byte[] item = new byte[5];
            System.arraycopy(result, 2 + (i - 1) * 5, item, 0, item.length);
            int id = item[0] & 0xff;
            Physicaltem physicaltem = allList.get(id);
//            if (physicaltem != null) {
//                normalList.add(physicaltem);
//            }
            switch (id) {
                case 1: // 电脑(ECU)中存储的故障码数量  1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */String.valueOf(item[1] & 0x7f));
                    normalList.add(physicaltem);
                    break;
                case 2: // 冻结故障码 1
                    String type = "";
                    switch ((item[1] & 0xFF) >> 6) {
                        case 0:
                            type = "P";
                            break;
                        case 1:
                            type = "C";
                            break;
                        case 2:
                            type = "B";
                            break;
                        case 3:
                            type = "U";
                            break;
                    }
                    type += HexUtils.byte2HexStr(new byte[]{(byte) (item[1] & 0x3F), item[2]}).replace(" ", "");
                    physicaltem.setCurrent(type);
                    normalList.add(physicaltem);
                    break;
                case 3: // 燃油系统1状态  6
//                    physicaltem = allList.get(301);
//                    switch ((item[1] & 0xff)) {
//                        case 1:
//                            physicaltem.setCurrent("OBD II(California ARB)");
//                            break;
//                        case 2:
//                            physicaltem.setCurrent("OBD(Federal EPA)");
//                            break;
//                        case 3:
//                            physicaltem.setCurrent("OBD and OBD II");
//                            break;
//                        case 4:
//                            physicaltem.setCurrent("OBD I ");
//                            break;
//                        case 5:
//                            physicaltem.setCurrent("Not intended to meet any OBD requirements");
//                            break;
//                        case 6:
//                            physicaltem.setCurrent("EOBD(Europe)");
//                            break;
//                    }
//                    normalList.add(physicaltem);
//
//                    // 燃油系统2状态
//                    physicaltem = allList.get(302);
//                    normalList.add(physicaltem);
                    break;
                case 4: // 发动机负荷 1
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff) * 100) / 255f));
                    normalList.add(physicaltem);
                    break;
                case 5: // 发动机冷却液温度 5
                    physicaltem.setCurrent(String.valueOf((item[1] & 0xff) - 40));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf(((item[1] & 0xff)) - 40));
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    status[4] = physicaltem.getStyle() != 1 ? status[4] : false;
                    break;
                case 6: // 短期燃油调整（缸组1） 6
                case 7: // 长期燃油调整（缸组1） 6
                case 8: // 短期燃油调整（缸组2） 6
                case 9: // 长期燃油调整（缸组2） 6
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff) * 100 / 128) - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf((item[1] & 0xff) * 100 / 128f - 100));
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    break;
                case 10: // 油轨油压 6
                    physicaltem.setCurrent(String.valueOf((item[1] & 0xff) * 3));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf((item[1] & 0xff) * 3));
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    break;
                case 11: // 进气管绝对压力 6
                    physicaltem.setCurrent(String.valueOf((item[1] & 0xff)));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf((item[1] & 0xff)));
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    break;
                case 12: // 引擎转速 1
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 4f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + HexUtils.byte2HexStr(item[2]) + "  " + String.valueOf((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 4));
                    status[0] = physicaltem.getStyle() != 1 ? status[0] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 13: // 车速 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " +*/ String.valueOf((item[1] & 0xff)));
                    normalList.add(physicaltem);
                    break;
                case 14: // 点火提前角 2
                    physicaltem.setCurrent(String.valueOf((item[1] & 0xff) / 2 - 64));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf((item[1] & 0xff) / 2f - 64));
                    status[1] = physicaltem.getStyle() != 1 ? status[1] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 15: // 进气温度 6
                    physicaltem.setCurrent(String.valueOf((item[1] & 0xff) - 40));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf((item[1] & 0xff) - 40));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 16: // MAF(空气质量流量)空气流速 6
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 100f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + String.valueOf((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 17: // 节气门位置 6
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff) * 100) / 255f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf((item[1] & 0xff) * 100 / 255f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 18: // 指令的二次空气喷射状态 7
                    switch ((item[1] & 0xff)) {
                        case 1:
                            physicaltem.setCurrent("Upstream");
                            break;
                        case 2:
                            physicaltem.setCurrent("Downstream of catalytic converter");
                            break;
                        case 4:
                            physicaltem.setCurrent("From the outside atmosphere or off");
                            break;
                        case 8:
                            physicaltem.setCurrent("Pump commanded on for diagnostics");
                            break;
                    }
                    normalList.add(physicaltem);
                    break;
                case 19: // 氧传感器当前状态 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */String.valueOf((item[1] & 0xff)));
                    normalList.add(physicaltem);
                    break;
                case 20: // 6
                    // 氧传感器输出电压（缸组1 传感器1)(v)
                    physicaltem = allList.get(201);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组1 传感器1）
                    physicaltem = allList.get(202);
                    physicaltem.setCurrent(decimalFormat.format((((item[1] & 0xff)) * 100 / 128f) - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + String.valueOf(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 21: // 6
                    // 氧传感器输出电压（缸组1 传感器2)(v)
                    physicaltem = allList.get(211);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组1 传感器2）
                    physicaltem = allList.get(212);
                    physicaltem.setCurrent(decimalFormat.format((((item[1] & 0xff) * 100) / 128) - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 22: // 6
                    // 氧传感器输出电压（缸组1 传感器3)(v)
                    physicaltem = allList.get(221);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组1 传感器3）
                    physicaltem = allList.get(222);
                    physicaltem.setCurrent(decimalFormat.format((((item[1] & 0xff)) * 100 / 128) - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 23: // 6
                    // 氧传感器输出电压（缸组1 传感器4)(v)
                    physicaltem = allList.get(231);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组1 传感器4）
                    physicaltem = allList.get(232);
                    physicaltem.setCurrent(decimalFormat.format((((item[1] & 0xff)) * 100 / 128) - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 24: // 6
                    // 氧传感器输出电压（缸组2 传感器1)(v)
                    physicaltem = allList.get(241);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组2 传感器1）
                    physicaltem = allList.get(242);
                    physicaltem.setCurrent(decimalFormat.format((((item[1] & 0xff)) * 100 / 128) - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 25: // 6
                    // 氧传感器输出电压（缸组2 传感器2)(v)
                    physicaltem = allList.get(251);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组2 传感器2）
                    physicaltem = allList.get(252);
                    physicaltem.setCurrent(decimalFormat.format((((item[1] & 0xff)) * 100) / 128 - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 26: // 6
                    // 氧传感器输出电压（缸组2 传感器3)(v)
                    physicaltem = allList.get(261);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组2 传感器3）
                    physicaltem = allList.get(262);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 100 / 128 - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 27: // 6
                    // 氧传感器输出电压（缸组2 传感器4)(v)
                    physicaltem = allList.get(271);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 0.005));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 短期燃油修正（缸组2 传感器4）
                    physicaltem = allList.get(272);
                    physicaltem.setCurrent(decimalFormat.format(((item[1] & 0xff)) * 100 / 128 - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format(((item[1] & 0xff)) * 100 / 128f - 100));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 28: // 当前所使用的OBD标准  1
//                    switch ((item[1] & 0xff)) {
//                        case 1:
//                            physicaltem.setCurrent("OBD II(California ARB)");
//                            break;
//                        case 2:
//                            physicaltem.setCurrent("OBD(Federal EPA)");
//                            break;
//                        case 3:
//                            physicaltem.setCurrent("OBD and OBD II");
//                            break;
//                        case 4:
//                            physicaltem.setCurrent("OBD I ");
//                            break;
//                        case 5:
//                            physicaltem.setCurrent("Not intended to meet any OBD requirements");
//                            break;
//                        case 6:
//                            physicaltem.setCurrent("EOBD(Europe)");
//                            break;
//                        default:
//                            break;
//                    }
//                    normalList.add(physicaltem);
                    break;
                case 29: // 氧传感器当前状态 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */decimalFormat.format(((item[1] & 0xff))));
                    normalList.add(physicaltem);
                    break;
                case 30: // 辅助输入状态 6
                    physicaltem.setCurrent(HexUtils.getBooleanArray(item[1])[7] == 0 ? "PTO not active" : "PTO active");
                    normalList.add(physicaltem);
                    break;
                case 31: // 引擎启动后的运行时间 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF)));
                    normalList.add(physicaltem);
                    break;
                case 33: // 故障指示灯(MIL)亮的情况下形式的距离 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF)));
                    normalList.add(physicaltem);
                    break;
                case 34: // 油轨压力(相对于歧管真空度) 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 0.079));
                    normalList.add(physicaltem);
                    break;
                case 35: // 高压油轨压力(直喷柴油或汽油压力) 6
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 10));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 10));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 36: // 6
                    // 氧传感器1线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(361);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器1线性或宽带式氧传感器电压
                    physicaltem = allList.get(362);
                    physicaltem.setCurrent(decimalFormat.format(((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f)));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format(((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f)));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 37: // 6
                    // 氧传感器2线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(371);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器2线性或宽带式氧传感器电压
                    physicaltem = allList.get(372);
                    physicaltem.setCurrent(decimalFormat.format((((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8) / 65536f)));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format(((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f)));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 38: // 6
                    // 氧传感器3线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(381);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器3线性或宽带式氧传感器电压
                    physicaltem = allList.get(382);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 39: // 6
                    // 氧传感器4线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(391);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器4线性或宽带式氧传感器电压
                    physicaltem = allList.get(392);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 40: // 6
                    // 氧传感器5线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(401);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器5线性或宽带式氧传感器电压
                    physicaltem = allList.get(402);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 41: // 6
                    // 氧传感器6线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(411);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器6线性或宽带式氧传感器电压
                    physicaltem = allList.get(412);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 42: // 6
                    // 氧传感器7线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(421);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器7线性或宽带式氧传感器电压
                    physicaltem = allList.get(422);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 43: // 6
                    // 氧传感器8线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(431);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器8线性或宽带式氧传感器电压
                    physicaltem = allList.get(432);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) * 8 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 44: // 设置废气再循环(Commanded EGR) 7
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 45: // 废气再循环误差 7
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */decimalFormat.format((item[1] & 0xff) * 100 / 128f - 100));
                    normalList.add(physicaltem);
                    break;
                case 46: // 可控蒸发净化 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */decimalFormat.format(((item[1] & 0xff)) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 47: // 燃油液位输入 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + "  " + */decimalFormat.format(((item[1] & 0xff)) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 49: // 故障码清除后的行驶里程 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF)));
                    normalList.add(physicaltem);
                    break;
                case 50: // 燃油蒸气排放系统蒸气绝对压力 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 4f));
                    normalList.add(physicaltem);
                    break;
                case 51: // 大气压 1
                    physicaltem.setCurrent(decimalFormat.format((item[1] & 0xff)));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + "  " + decimalFormat.format((item[1] & 0xff)));
                    status[0] = physicaltem.getStyle() != 1 ? status[0] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 52: // 6
                    // 氧传感器1线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(521);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器1线性或宽带式氧传感器电流
                    physicaltem = allList.get(522);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128f));
                    normalList.add(physicaltem);
                    break;
                case 53: // 6
                    // 氧传感器2线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(531);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器2线性或宽带式氧传感器电流
                    physicaltem = allList.get(532);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128));
                    normalList.add(physicaltem);
                    break;
                case 54: // 6
                    // 氧传感器3线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(541);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器3线性或宽带式氧传感器电流
                    physicaltem = allList.get(542);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256 - 128));
                    normalList.add(physicaltem);
                    break;
                case 55: // 6
                    // 氧传感器4线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(551);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器4线性或宽带式氧传感器电流
                    physicaltem = allList.get(552);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " +*/ decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128));
                    normalList.add(physicaltem);
                    break;
                case 56: // 6
                    // 氧传感器5线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(561);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器5线性或宽带式氧传感器电流
                    physicaltem = allList.get(562);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " +*/ decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128));
                    normalList.add(physicaltem);
                    break;
                case 57: // 6
                    // 氧传感器6线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(571);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器6线性或宽带式氧传感器电流
                    physicaltem = allList.get(572);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " +*/ decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128));
                    normalList.add(physicaltem);
                    break;
                case 58: // 6
                    // 氧传感器7线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(581);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器7线性或宽带式氧传感器电流
                    physicaltem = allList.get(582);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " +*/ decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128));
                    normalList.add(physicaltem);
                    break;
                case 59: // 6
                    // 氧传感器8线性或宽带式氧传感器 当量比（λ）
                    physicaltem = allList.get(591);
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65536f));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    // 氧传感器8线性或宽带式氧传感器电流
                    physicaltem = allList.get(592);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[3]) + HexUtils.byte2HexStr(item[4]) + "  " +*/ decimalFormat.format((HexUtils.byteToShort(new byte[]{item[3], item[4]}) & 0xFFFF) / 256f - 128));
                    normalList.add(physicaltem);
                    break;
                case 60: // 缸组 1的1号传感器催化剂温度 7
                case 61: // 缸组 2的1号传感器催化剂温度 7
                case 62: // 缸组 1的2号传感器催化剂温度 7
                case 63: // 缸组 2的2号传感器催化剂温度 7
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 10 - 40));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 10f - 40));
                    status[6] = physicaltem.getStyle() != 1 ? status[6] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 66: // 控制模块电压 3
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 1000f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 1000f));
                    status[2] = physicaltem.getStyle() != 1 ? status[2] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 67: // 绝对负载 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 100 / 255));
                    normalList.add(physicaltem);
                    break;
                case 68: // 可控当量比 1
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65535f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 2 / 65535f));
                    status[0] = physicaltem.getStyle() != 1 ? status[0] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 69: // 节气门相对位置 1
                    physicaltem.setCurrent(decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + " " + decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    break;
                case 70: // 环境空气温度 1
                    physicaltem.setCurrent(decimalFormat.format((item[1] & 0xff) - 40));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + " " + decimalFormat.format((item[1] & 0xff) - 40));
                    status[0] = physicaltem.getStyle() != 1 ? status[0] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 71: // 节气门绝对位置 B 1
                case 72: // 节气门绝对位置 C 1
                case 73: // 油门踏板位置 D 1
                case 74: // 油门踏板位置 E 1
                case 75: // 油门踏板位置 F 1
                case 76: // 操作节气门制动器 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 77: // 故障指示灯亮起后的运行时间 1
                case 78: // 故障码被清理后的运行时间 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF)));
                    normalList.add(physicaltem);
                    break;
                case 81: // 燃油类型 6
                    break;
                case 82: // 乙醇燃料百分比 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 83: // 蒸发冷却系统绝对蒸汽压 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 200f));
                    normalList.add(physicaltem);
                    break;
                case 84: // 蒸发冷却系统蒸汽压 6
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) - 32768));
                    normalList.add(physicaltem);
                    break;
                case 85: // 6
                    // 短周期缸组1二次氧传感器燃油调整
                    physicaltem = allList.get(851);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);

                    // 短周期缸组3二次氧传感器燃油调整
                    physicaltem = allList.get(852);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);
                    break;
                case 86: // 6
                    // 长周期缸组1二次氧传感器燃油调整
                    physicaltem = allList.get(861);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);

                    // 长周期缸组3二次氧传感器燃油调整
                    physicaltem = allList.get(862);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);
                    break;
                case 87: // 6
                    // 短周期缸组2二次氧传感器燃油调整
                    physicaltem = allList.get(871);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);

                    // 短周期缸组4二次氧传感器燃油调整
                    physicaltem = allList.get(872);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);
                    break;
                case 88: // 6
                    // 长周期缸组2二次氧传感器燃油调整
                    physicaltem = allList.get(881);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);

                    // 长周期缸组4二次氧传感器燃油调整
                    physicaltem = allList.get(882);
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) / 128f - 100));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
                    normalList.add(physicaltem);
                    break;
                case 89: // 油轨压力(绝对压力) 6
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 10));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) * 10));
                    status[5] = physicaltem.getStyle() != 1 ? status[5] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 90: // 油门踏板相对位置 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 91: // 混合动力电池组的剩余寿命 3
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " + */decimalFormat.format((item[1] & 0xff) * 100 / 255f));
                    normalList.add(physicaltem);
                    break;
                case 92: // 引擎润滑油温 4
                    physicaltem.setCurrent(decimalFormat.format((item[1] & 0xff) - 40));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + " " + decimalFormat.format((item[1] & 0xff) - 40));
                    status[3] = physicaltem.getStyle() != 1 ? status[3] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 93: // 喷油提前角 1
                    physicaltem.setCurrent(decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 128f - 210));
                    physicaltem.setStyle((physicaltem.getMin() <= Double.valueOf(physicaltem.getCurrent()) && Double.valueOf(physicaltem.getCurrent()) <= physicaltem.getMax()) ? 0 : 1);
//                    physicaltem.setCurrent(HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 128f - 210));
                    status[0] = physicaltem.getStyle() != 1 ? status[0] : false;
                    if (physicaltem.getStyle() == 1) {
                        errorPhysicalList.add(physicaltem);
                    } else {
                        normalList.add(physicaltem);
                    }
                    break;
                case 94: // 发动机燃油消耗率 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF) / 20));
                    normalList.add(physicaltem);
                    break;
                case 97: // 驾驶者需求的引擎转矩百分比 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " +*/ decimalFormat.format((item[1] & 0xff) - 125));
                    normalList.add(physicaltem);
                    break;
                case 98: // 引擎的实际转矩百分比 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + " " +*/ decimalFormat.format((item[1] & 0xff) - 125));
                    normalList.add(physicaltem);
                    break;
                case 99: // 引擎参考转矩 1
                    physicaltem.setCurrent(/*HexUtils.byte2HexStr(item[1]) + HexUtils.byte2HexStr(item[2]) + "  " + */decimalFormat.format((HexUtils.byteToShort(new byte[]{item[1], item[2]}) & 0xFFFF)));
                    normalList.add(physicaltem);
                    break;
                case 100:// 引擎转矩百分比数据信息 1
                    break;
                default:
                    break;
            }
        }
        physical.setStatus(status[index] ? "1" : "2");
        if (types.length - 1 == index) {
            normalAdapter.notifyDataSetChanged();
        } else {
            physicalList.get(index + 1).setStatus("0");// 最后一项
            normalAdapter.notifyItemRangeChanged(index, 2);
        }
    }

    public static class VH extends RecyclerView.ViewHolder {
        public final TextView type;
        public final TextView checking;
        public final TextView normal;
        public final TextView error;

        public VH(View v) {
            super(v);
            type = v.findViewById(R.id.type);
            checking = v.findViewById(R.id.checking);
            normal = v.findViewById(R.id.normal);
            error = v.findViewById(R.id.error);
        }
    }

    public class NormalAdapter extends RecyclerView.Adapter<VH> {

        private List<Physical> mDatas;

        public NormalAdapter(List<Physical> data) {
            this.mDatas = data;
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            Physical physical = mDatas.get(position);
            holder.type.setText(physical.getName());
            if ("0".equals(physical.getStatus())) {
                holder.checking.setVisibility(View.VISIBLE);
                holder.normal.setVisibility(View.GONE);
                holder.error.setVisibility(View.GONE);
            } else if ("1".equals(physical.getStatus())) {
                holder.normal.setVisibility(View.VISIBLE);
                holder.checking.setVisibility(View.GONE);
                holder.error.setVisibility(View.GONE);
            } else if ("2".equals(physical.getStatus())) {
                holder.error.setVisibility(View.VISIBLE);
                holder.checking.setVisibility(View.GONE);
                holder.normal.setVisibility(View.GONE);
            } else {
                holder.checking.setVisibility(View.GONE);
                holder.normal.setVisibility(View.GONE);
                holder.error.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.physical_item, parent, false);
            return new VH(v);
        }
    }
}
