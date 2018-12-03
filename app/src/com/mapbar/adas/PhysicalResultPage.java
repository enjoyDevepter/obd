package com.mapbar.adas;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.view.CustomExpandableListView;
import com.miyuan.obd.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@PageSetting(contentViewId = R.layout.physical_result_layout)
public class PhysicalResultPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.home)
    private View homeV;
    @ViewInject(R.id.errorlistView)
    private CustomExpandableListView errorlistView;
    @ViewInject(R.id.normallistView)
    private CustomExpandableListView normallistView;

    @Override
    public void onResume() {
        super.onResume();
        titleTV.setText("体检结果");
        back.setOnClickListener(this);
        homeV.setOnClickListener(this);
        reportV.setVisibility(View.GONE);

        List<Physicaltem> errorLists = getDate().<Physicaltem>getParcelableArrayList("error");
        PhysicalErrorAdapter physicalErrorAdapter = new PhysicalErrorAdapter(errorLists);
        errorlistView.setDivider(new ColorDrawable(0xFFF4F4F4));
        errorlistView.setDividerHeight(8);
        errorlistView.setAdapter(physicalErrorAdapter);

        HashMap<String, List<Physicaltem>> physicaltemHashMap = new HashMap<>();
        List<Physicaltem> normalListItems = getDate().<Physicaltem>getParcelableArrayList("normal");
        for (Physicaltem physicaltem : normalListItems) {
            if (physicaltemHashMap.containsKey(physicaltem.getType().trim())) {
                physicaltemHashMap.get(physicaltem.getType()).add(physicaltem);
            } else {
                List<Physicaltem> items = new ArrayList<>();
                items.add(physicaltem);
                physicaltemHashMap.put(physicaltem.getType(), items);
            }
        }
        PhysicalNormalAdapter physicalNormalAdapter = new PhysicalNormalAdapter(physicaltemHashMap);
        normallistView.setDivider(new ColorDrawable(0xFFF4F4F4));
        normallistView.setDividerHeight(8);
        normallistView.setAdapter(physicalNormalAdapter);
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
            case R.id.home:
                PageManager.go(new HomePage());
                break;
        }
    }
}
