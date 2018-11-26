package com.mapbar.adas;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

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
    @ViewInject(R.id.content)
    private RecyclerView contentLV;
    private NormalAdapter normalAdapter;

    @Override
    public void onResume() {
        super.onResume();
        titleTV.setText("体检结果");
        back.setOnClickListener(this);
        homeV.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
        contentLV.setLayoutManager(new LinearLayoutManager(getContext()));
        normalAdapter = new NormalAdapter(getDate().<Physicaltem>getParcelableArrayList("result"));
        contentLV.setAdapter(normalAdapter);
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

    public static class VH extends RecyclerView.ViewHolder {
        public final TextView type;
        public final TextView name;
        public final TextView status;
        public final TextView current;
        public final TextView range;
        public final View errorlayout;
        public final TextView reason;
        public final TextView resolvent;

        public VH(View v) {
            super(v);
            type = v.findViewById(R.id.type);
            name = v.findViewById(R.id.name);
            status = v.findViewById(R.id.status);
            current = v.findViewById(R.id.current);
            range = v.findViewById(R.id.range);
            errorlayout = v.findViewById(R.id.error_layout);
            reason = v.findViewById(R.id.reason);
            resolvent = v.findViewById(R.id.resolvent);
        }
    }

    public class NormalAdapter extends RecyclerView.Adapter<VH> {

        private List<Physicaltem> mDatas;

        public NormalAdapter(List<Physicaltem> data) {
            this.mDatas = data;
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            Physicaltem physical = mDatas.get(position);
            holder.type.setText(physical.getType());
            holder.name.setText(physical.getName());
            holder.current.setText(physical.getCurrent());
            holder.range.setText(physical.getMin() + "-" + physical.getMax());
            holder.current.setText(physical.getCurrent());
            switch (physical.getStyle()) {
                case 0:
                    holder.status.setVisibility(View.INVISIBLE);
                    holder.errorlayout.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.status.setVisibility(View.VISIBLE);
                    holder.errorlayout.setVisibility(View.VISIBLE);
                    if (physical.isHigh()) {
                        holder.reason.setText(Html.fromHtml(physical.getHigt_reason()));
                        holder.resolvent.setText(Html.fromHtml(physical.getHigt_resolvent()));
                    } else {
                        holder.reason.setText(Html.fromHtml(physical.getLow_reason()));
                        holder.resolvent.setText(Html.fromHtml(physical.getLow_resolvent()));
                    }
                    break;
                case 2:
                    holder.status.setVisibility(View.INVISIBLE);
                    holder.errorlayout.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.physical_result_item, parent, false);
            return new VH(v);
        }
    }
}
