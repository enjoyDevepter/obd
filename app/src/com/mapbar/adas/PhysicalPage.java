package com.mapbar.adas;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.ArrayList;
import java.util.List;

@PageSetting(contentViewId = R.layout.physical_layout)
public class PhysicalPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    String[] types = new String[]{"发动机控制系统", "点火控制系统", "供电控制系统", "润滑控制系统", "冷却控制系统", "燃油及空气系统", "排放控制系统"};
    int[] counts = new int[]{30, 1, 2, 1, 7, 79, 1};
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private View confirmV;
    @ViewInject(R.id.content)
    private ListView contentLV;
    private List<Physical> physicalList = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        BlueManager.getInstance().addBleCallBackListener(this);
        titleTV.setText("爱车体检");
        back.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
        initPhysical();
        contentLV.setAdapter(new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return physicalList.size();
            }

            @Override
            public Object getItem(int position) {
                return physicalList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.physical_item, null);
                ((TextView) view.findViewById(R.id.type)).setText(physicalList.get(position).getName());
                ((TextView) view.findViewById(R.id.count)).setText("共" + physicalList.get(position).getCount() + "项");
                return view;
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        });
    }

    private void initPhysical() {
        for (int i = 0; i < counts.length; i++) {
            Physical physical = new Physical();
            physical.setName(types[i]);
            physical.setCount(counts[i]);
            physicalList.add(physical);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                BlueManager.getInstance().send(ProtocolUtils.sendPhysical(01));
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {

    }
}
