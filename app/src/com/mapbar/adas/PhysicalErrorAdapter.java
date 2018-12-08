package com.mapbar.adas;

import android.text.Html;
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

public class PhysicalErrorAdapter extends BaseExpandableListAdapter {

    private List<Physicaltem> physicaltems;

    public PhysicalErrorAdapter(List<Physicaltem> physicaltems) {
        this.physicaltems = physicaltems;
    }

    @Override
    public int getGroupCount() {
        return physicaltems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return physicaltems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return physicaltems.get(groupPosition);
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
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.physical_result_error_parent_item, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.name = convertView.findViewById(R.id.name);
            groupViewHolder.status = convertView.findViewById(R.id.status);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.name.setText(physicaltems.get(groupPosition).getName());
        groupViewHolder.status.setText(Double.valueOf(physicaltems.get(groupPosition).getCurrent()) > physicaltems.get(groupPosition).getMax() ? "偏高" : "偏低");
        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.physical_result_error_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.type = convertView.findViewById(R.id.type);
            childViewHolder.current = convertView.findViewById(R.id.current);
            childViewHolder.range = convertView.findViewById(R.id.range);
            childViewHolder.reason = convertView.findViewById(R.id.reason);
            childViewHolder.resolvent = convertView.findViewById(R.id.resolvent);
            childViewHolder.up = convertView.findViewById(R.id.up);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        childViewHolder.type.setText(physicaltems.get(groupPosition).getType());
        if (null != physicaltems.get(groupPosition).getCurrent() && !"".equals(physicaltems.get(groupPosition).getCurrent()) && !"null".equals(physicaltems.get(groupPosition).getCurrent())) {
            childViewHolder.current.setText(physicaltems.get(groupPosition).getCurrent() + " " + physicaltems.get(groupPosition).getUnit());
        } else {
            childViewHolder.current.setText("");
        }
        childViewHolder.up.setBackgroundResource(Double.valueOf(physicaltems.get(groupPosition).getCurrent()) > physicaltems.get(groupPosition).getMax() ? R.drawable.high : R.drawable.low);
        childViewHolder.range.setText(physicaltems.get(groupPosition).getMin() + "-" + physicaltems.get(groupPosition).getMax());
        if (physicaltems.get(groupPosition).isHigh()) {
            childViewHolder.reason.setText(Html.fromHtml(physicaltems.get(groupPosition).getHigt_reason()));
            childViewHolder.resolvent.setText(Html.fromHtml(physicaltems.get(groupPosition).getHigt_resolvent()));
        } else {
            childViewHolder.reason.setText(Html.fromHtml(physicaltems.get(groupPosition).getLow_reason()));
            childViewHolder.resolvent.setText(Html.fromHtml(physicaltems.get(groupPosition).getLow_resolvent()));
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView name;
        TextView status;
    }

    static class ChildViewHolder {
        TextView type;
        TextView current;
        TextView range;
        TextView reason;
        TextView resolvent;
        View up;
    }
}