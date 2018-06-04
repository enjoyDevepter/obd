package com.mapbar.adas;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CarHelper;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.adas.view.IndexSideBar;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

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
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.back)
    private View back;

    private CarAdapter carAdapter;
    private CarBrandExpandableListAdapter carBrandExpandableListAdapter;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("选择车型");
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        getCar();
    }

    private void getCar() {
        final Request request = new Request.Builder().url(URLUtils.GET_CAR_BRAND).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                try {
                    JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        final List<CarInfo> carInfos = (ArrayList<CarInfo>) JSON.parseArray(result.optString("brands"), CarInfo.class);
                        Log.d(carInfos.toString());
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                show(carInfos);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.next:
                PageManager.go(new MainPage());
                break;
        }
    }
}
