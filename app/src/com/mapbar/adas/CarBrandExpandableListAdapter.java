package com.mapbar.adas;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.miyuan.obd.R;

import java.util.List;

/**
 * Created by guomin on 2018/6/4.
 */

public class CarBrandExpandableListAdapter extends BaseExpandableListAdapter {

    private List<CarModel> carModels;

    public CarBrandExpandableListAdapter(List<CarModel> carModels) {
        this.carModels = carModels;
    }

    public List<CarModel> getCarModels() {
        return carModels;
    }

    public void setCarModels(List<CarModel> carModels) {
        this.carModels = carModels;
    }

    @Override
    public int getGroupCount() {
        return carModels.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<CarStyle> carStyleList = carModels.get(groupPosition).getStyles();
        return carStyleList == null ? 0 : carStyleList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return carModels.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<CarStyle> carStyleList = carModels.get(groupPosition).getStyles();
//        return carStyleList == null ? 0 : carStyleList.size();
        return carStyleList == null ? null : carStyleList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.model_item, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTitle = convertView.findViewById(R.id.car_name);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.tvTitle.setText(carModels.get(groupPosition).getName());
        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.model_style_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvTitle = convertView.findViewById(R.id.car_name);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        CarStyle style = carModels.get(groupPosition).getStyles().get(childPosition);
        childViewHolder.tvTitle.setTextColor(style.isChoice() ? Color.RED : Color.parseColor("#FF5D5D5D"));
        childViewHolder.tvTitle.setText(style.getName());
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView tvTitle;
    }

    static class ChildViewHolder {
        TextView tvTitle;
    }
}