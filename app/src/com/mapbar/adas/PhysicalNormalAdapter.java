package com.mapbar.adas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.miyuan.obd.R;

import java.util.List;
import java.util.Map;

/**
 * Created by guomin on 2018/6/4.
 */

public class PhysicalNormalAdapter extends BaseExpandableListAdapter {

    private Map<String, List<Physicaltem>> physicaltems;

    public PhysicalNormalAdapter(Map<String, List<Physicaltem>> physicaltems) {
        this.physicaltems = physicaltems;
    }

    @Override
    public int getGroupCount() {
        return physicaltems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return physicaltems.get(physicaltems.keySet().toArray()[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return physicaltems.keySet().toArray()[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return physicaltems.get(physicaltems.keySet().toArray()[groupPosition]).get(childPosition);
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
        String name = (String) physicaltems.keySet().toArray()[groupPosition];
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.physical_result_normal_parent_item, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.icon = convertView.findViewById(R.id.icon);
            groupViewHolder.type = convertView.findViewById(R.id.type);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        int iconId = R.drawable.checker_icon;
        switch (groupPosition) {
            case 0:
                iconId = R.drawable.checker_icon;
                break;
            case 1:
                iconId = R.drawable.checker_icon;
                break;
            case 2:
                iconId = R.drawable.checker_icon;
                break;
            case 3:
                iconId = R.drawable.checker_icon;
                break;
            case 4:
                iconId = R.drawable.checker_icon;
                break;
            case 5:
                iconId = R.drawable.checker_icon;
                break;
            case 6:
                iconId = R.drawable.checker_icon;
                break;
        }
//        groupViewHolder.icon.setBackgroundResource(iconId);
        groupViewHolder.type.setText(name);
        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Physicaltem physicaltem = (Physicaltem) getChild(groupPosition, childPosition);
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.physical_result_normal_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.current = convertView.findViewById(R.id.current);
            childViewHolder.range = convertView.findViewById(R.id.range);
            childViewHolder.name = convertView.findViewById(R.id.name);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        childViewHolder.name.setText(physicaltem.getName());
        childViewHolder.current.setText(physicaltem.getCurrent());
        childViewHolder.range.setText(physicaltem.getMin() + "-" + physicaltem.getMax());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        View icon;
        TextView type;
    }

    static class ChildViewHolder {
        TextView name;
        TextView current;
        TextView range;
    }
}