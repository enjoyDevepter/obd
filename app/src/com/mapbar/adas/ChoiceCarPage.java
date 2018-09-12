package com.mapbar.adas;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CarHelper;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.adas.view.IndexSideBar;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.choice_car_layout)
public class ChoiceCarPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.brand)
    ListView listView;
    @ViewInject(R.id.index_letter)
    IndexSideBar indexSideBar;
    @ViewInject(R.id.expandablelistView)
    ExpandableListView expandableListView;
    String carName = "";
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

    @Override
    public void onResume() {
        super.onResume();
        title.setText("选择车型");
        next.setOnClickListener(this);
        reportV.setOnClickListener(this);
        back.setOnClickListener(this);
        getCar();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void getCar() {
        showProgress();
        final Request request = new Request.Builder().url(URLUtils.GET_CAR_BRAND).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
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
                                                showProgress();
                                                confirm.setEnabled(false);
                                                getCar();
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
                dismissProgress();
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        carInfos = JSON.parseArray(result.optString("brands"), CarInfo.class);
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                show(carInfos);
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

    private void show(List<CarInfo> result) {

        final List<CarInfo> carInfos = CarHelper.setupContactInfoList(result);

        // 设置侧边栏中的字母索引
        List<String> mLetterIndexList = CarHelper.setupLetterIndexList(carInfos);
        indexSideBar.setLetterIndexList(mLetterIndexList, false);

        // 设置联系人列表的信息
        carInfos.get(0).setChoice(true);
        carAdapter = new CarAdapter(getContext(), carInfos);
        listView.setAdapter(carAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<CarModel> models = carInfos.get(position).getModels();

                for (CarModel model : models) {
                    for (CarStyle carStyle : model.getStyles()) {
                        carStyle.setChoice(false);
                    }
                }

                for (int i = 0; i < carInfos.size(); i++) {
                    if (i != position) {
                        carInfos.get(i).setChoice(false);
                    } else {
                        carInfos.get(i).setChoice(!carInfos.get(i).isChoice());
                    }
                }

                carAdapter.notifyDataSetChanged();
                carBrandExpandableListAdapter.setCarModels(models);
                carBrandExpandableListAdapter.notifyDataSetChanged();
            }
        });

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

        carBrandExpandableListAdapter = new CarBrandExpandableListAdapter(carInfos.get(0).getModels());
        expandableListView.setAdapter(carBrandExpandableListAdapter);
        expandableListView.setDivider(new ColorDrawable(0xfff8f9fa));
        expandableListView.setChildDivider(new ColorDrawable(0xfff));
        expandableListView.setDividerHeight(2);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (expandableListView == null) return;
                for (int i = 0; i < carBrandExpandableListAdapter.getGroupCount(); i++) {
                    if (i != groupPosition) {
                        expandableListView.collapseGroup(i);
                        CarModel carModel = (CarModel) carBrandExpandableListAdapter.getGroup(i);
                        for (CarStyle style : carModel.getStyles()) {
                            style.setChoice(false);
                        }
                    }
                }
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
                carBrandExpandableListAdapter.notifyDataSetChanged();
                return true;
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
                break;
        }
    }

    private void activate() {
        if (null == carInfos) {
            return;
        }

        String carId = "";

        for (CarInfo carInfo : carInfos) {
            if (carInfo.isChoice()) {
                for (CarModel carModel : carInfo.getModels()) {
                    for (CarStyle carStyle : carModel.getStyles()) {
                        if (carStyle.isChoice()) {
                            carId = carStyle.getId();
                            carName = carModel.getName() + "  " + carStyle.getName();
                        }
                    }
                }
            }
        }

        if (GlobalUtil.isEmpty(carId)) {
            Toast.makeText(getContext(), "请选择车型", Toast.LENGTH_LONG).show();
            return;
        }

        // 跳转到车型确认界面
        ConfirmCarPage confirmCarPage = new ConfirmCarPage();
        Bundle bundle = new Bundle();
        bundle.putString("boxId", getDate().getString("boxId"));
        bundle.putString("phone", getDate().getString("phone"));
        bundle.putString("code", getDate().getString("code"));
        bundle.putString("sn", getDate().getString("sn").toString());
        bundle.putString("carId", carId);
        bundle.putString("carName", carName);
        confirmCarPage.setDate(bundle);
        PageManager.go(confirmCarPage);
    }
}
