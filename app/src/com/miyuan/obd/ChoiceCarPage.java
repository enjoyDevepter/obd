package com.miyuan.obd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.log.FileLoggingTree;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.utils.CarHelper;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;
import com.miyuan.obd.utils.URLUtils;
import com.miyuan.obd.view.IndexSideBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.miyuan.obd.preferences.SettingPreferencesConfig.SN;

@PageSetting(contentViewId = R.layout.choice_car_layout)
public class ChoiceCarPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.brand)
    ListView listView;
    @ViewInject(R.id.index_letter)
    IndexSideBar indexSideBar;
    @ViewInject(R.id.expandablelistView)
    ExpandableListView expandableListView;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    private CarAdapter carAdapter;
    private CarBrandExpandableListAdapter carBrandExpandableListAdapter;
    private List<CarInfo> carInfos;
    private CustomDialog dialog;
    private String carId;
    private String carName = "";

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title.setText("选择车型");
        next.setOnClickListener(this);
        reportV.setOnClickListener(this);
        back.setOnClickListener(this);
        getCarBrands();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 获取品牌
     */
    private void getCarBrands() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "");
            jsonObject.put("type", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("getCarBrands input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        final Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.GET_CAR_BRAND).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        ((TextView) (view.findViewById(R.id.confirm))).setText("已打开网络，重试");
                                        ((TextView) (view.findViewById(R.id.info))).setText("请打开网络，否则无法完成当前操作!");
                                        ((TextView) (view.findViewById(R.id.title))).setText("网络异常");
                                        final View confirm = view.findViewById(R.id.confirm);
                                        confirm.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                confirm.setEnabled(false);
                                                getCarBrands();
                                            }
                                        });
                                    }
                                })
                                .setLayoutRes(R.layout.dailog_common_warm)
                                .setCancelOutside(false)
                                .setDimAmount(0.5f)
                                .isCenter(true)
                                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                                .show();
                    }
                });

            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getCarBrands success ");
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        carInfos = JSON.parseArray(result.optString("brands"), CarInfo.class);
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                showBrands(carInfos);
                                getCarModelForBrands(carInfos.get(0).getId());
                            }
                        });
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void getCarModelForBrands(final String id) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("type", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("getCarModelForBrands input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        final Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.GET_CAR_BRAND).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("getCarModelForBrands failure " + e.getMessage());
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        ((TextView) (view.findViewById(R.id.confirm))).setText("已打开网络，重试");
                                        ((TextView) (view.findViewById(R.id.info))).setText("请打开网络，否则无法完成当前操作!");
                                        ((TextView) (view.findViewById(R.id.title))).setText("网络异常");
                                        final View confirm = view.findViewById(R.id.confirm);
                                        confirm.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                getCarModelForBrands(id);
                                            }
                                        });
                                    }
                                })
                                .setLayoutRes(R.layout.dailog_common_warm)
                                .setCancelOutside(false)
                                .setDimAmount(0.5f)
                                .isCenter(true)
                                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                                .show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getCarModelForBrands success ");
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        carInfos = JSON.parseArray(result.optString("brands"), CarInfo.class);
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                showModle(carInfos.get(0).getModels());
                            }
                        });
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getCarStyleForModel(final String id, final int groupIndex) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("type", "3");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("getCarStyleForModel input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        final Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.GET_CAR_BRAND).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("getCarStyleForModel failure " + e.getMessage());
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        ((TextView) (view.findViewById(R.id.confirm))).setText("已打开网络，重试");
                                        ((TextView) (view.findViewById(R.id.info))).setText("请打开网络，否则无法完成当前操作!");
                                        ((TextView) (view.findViewById(R.id.title))).setText("网络异常");
                                        final View confirm = view.findViewById(R.id.confirm);
                                        confirm.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                getCarStyleForModel(id, groupIndex);
                                            }
                                        });
                                    }
                                })
                                .setLayoutRes(R.layout.dailog_common_warm)
                                .setCancelOutside(false)
                                .setDimAmount(0.5f)
                                .isCenter(true)
                                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                                .show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getCarStyleForModel success ");
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        carInfos = JSON.parseArray(result.optString("brands"), CarInfo.class);
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                CarModel carModel = carBrandExpandableListAdapter.getCarModels().get(groupIndex);
                                carModel.setStyles(null);
                                carModel.setStyles(carInfos.get(0).getModels().get(0).getStyles());
                                carBrandExpandableListAdapter.notifyDataSetChanged();
//                                showModle(carInfos);
                            }
                        });
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void showModle(List<CarModel> result) {
        if (null == result || (result != null && result.size() <= 0)) {
            return;
        }
        carBrandExpandableListAdapter = new CarBrandExpandableListAdapter(result);
        expandableListView.setAdapter(carBrandExpandableListAdapter);
        expandableListView.setDivider(new ColorDrawable(0xfff8f9fa));
        expandableListView.setChildDivider(new ColorDrawable(0xffff));
        expandableListView.setDividerHeight(2);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (expandableListView == null) return;
                for (int i = 0; i < carBrandExpandableListAdapter.getGroupCount(); i++) {
                    if (i != groupPosition) {
                        expandableListView.collapseGroup(i);
                    }
                }
                CarModel carModel = (CarModel) carBrandExpandableListAdapter.getGroup(groupPosition);
                getCarStyleForModel(carModel.getId(), groupPosition);
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                CarModel carModel = (CarModel) carBrandExpandableListAdapter.getGroup(groupPosition);
                CarStyle carStyle = carModel.getStyles().get(childPosition);
                carStyle.setChoice(!carStyle.isChoice());
                for (CarStyle style : carModel.getStyles()) {
                    if (style != carStyle) {
                        style.setChoice(false);
                    }
                }
                carId = carStyle.getId();
                carName = carModel.getName() + " " + carStyle.getName();
                carBrandExpandableListAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void showBrands(List<CarInfo> result) {

        final List<CarInfo> carInfos = CarHelper.setupContactInfoList(result);

        // 设置侧边栏中的字母索引
        List<String> mLetterIndexList = CarHelper.setupLetterIndexList(carInfos);
        indexSideBar.setLetterIndexList(mLetterIndexList, false);

        // 设置联系人列表的信息
        carInfos.get(0).setChoice(true);
        carAdapter = new CarAdapter(getContext(), carInfos);
        listView.setAdapter(carAdapter);

        // 设置侧边栏的触摸事件监听
        indexSideBar.setOnTouchLetterListener(new IndexSideBar.OnTouchLetterListener() {
            @Override
            public void onTouchingLetterListener(String letter) {
                int position = carAdapter.getPositionForSection(letter.charAt(0));
                if (position != -1) {
                    listView.setSelection(position);     // jump to the specified position
                }
            }

            @Override
            public void onTouchedLetterListener() {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getCarModelForBrands(carInfos.get(position).getId());
                for (int i = 0; i < carInfos.size(); i++) {
                    if (i != position) {
                        carInfos.get(i).setChoice(false);
                    } else {
                        carInfos.get(i).setChoice(!carInfos.get(i).isChoice());
                    }
                }
                carId = "";
                carName = "";

                carAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.next:
                activate();
                break;
            case R.id.report:
                showLogDailog();
                break;
        }
    }

    private void showLogDailog() {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) (view.findViewById(R.id.sn))).setText(SN.get());
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        uploadLog();
                                        dialog.dismiss();
                                    }
                                });
                                view.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //获取剪贴板管理器
                                        ClipboardManager cm = (ClipboardManager) GlobalUtil.getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        // 创建普通字符型ClipData
                                        ClipData mClipData = ClipData.newPlainText("Label", SN.get());
                                        // 将ClipData内容放到系统剪贴板里。
                                        cm.setPrimaryClip(mClipData);
                                    }
                                });
                            }
                        })
                        .setLayoutRes(R.layout.log_dailog)
                        .setCancelOutside(false)
                        .setDimAmount(0.5f)
                        .isCenter(true)
                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                        .show();
            }
        });
    }

    private void uploadLog() {
        Log.d("ChoiceCarPage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "log");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", getDate().getString("sn")))
                    .addPart(MultipartBody.Part.createFormData("type", "1"));
            for (File file : logs) {
                if (!file.getName().equals(FileLoggingTree.fileName)) {
                    builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
                }
            }
            Request request = new Request.Builder()
                    .url(URLUtils.UPDATE_ERROR_FILE)
                    .post(builder.build())
                    .build();

            GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("ChoiceCarPage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("ChoiceCarPage uploadLog success " + responese);
                    try {
                        final JSONObject result = new JSONObject(responese);
                        if ("000".equals(result.optString("status"))) {
                            GlobalUtil.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "上报成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            for (File delete : logs) {
                                if (!delete.getName().equals(FileLoggingTree.fileName)) {
                                    delete.delete();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("ChoiceCarPage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }

    private void activate() {
        if (null == carInfos) {
            return;
        }

        if (GlobalUtil.isEmpty(carId)) {
            Toast.makeText(getContext(), "请选择车型", Toast.LENGTH_LONG).show();
            return;
        }

        // 跳转到车型确认界面
        ConfirmCarPage confirmCarPage = new ConfirmCarPage();
        Bundle bundle = new Bundle();
        bundle.putString("boxId", getDate().getString("boxId"));
        bundle.putString("sn", getDate().getString("sn"));
        bundle.putString("carId", carId);
        bundle.putString("carName", carName);
        confirmCarPage.setDate(bundle);
        PageManager.go(confirmCarPage);
    }
}
