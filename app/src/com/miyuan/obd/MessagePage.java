package com.miyuan.obd;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;

import java.util.List;

@PageSetting(contentViewId = R.layout.message_layout)
public class MessagePage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.content)
    private RecyclerView contentLV;

    @Override
    public void onResume() {
        super.onResume();
        titleTV.setText("消息中心");
        back.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
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
