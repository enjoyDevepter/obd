package com.mapbar.adas;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mapbar.obd.R;

import java.util.List;

/**
 * Created by guomin on 2018/2/7.
 */

public class CarAdapter extends BaseAdapter implements SectionIndexer {

    private static boolean DEBUG = false;

    private LayoutInflater inflater;
    private List<CarInfo> carInfoList;

    public CarAdapter(Context context, List<CarInfo> list) {
        carInfoList = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return carInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return carInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.car_item, null);
            holder.letterTV = (TextView) convertView.findViewById(R.id.tv_contact_index_letter);
            holder.nameTV = (TextView) convertView.findViewById(R.id.car_name);
            holder.contentV = convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CarInfo carInfo = carInfoList.get(position);

        int section = getSectionForPosition(position);
        int startSectionPosition = getPositionForSection(section);
        if (position == startSectionPosition) {
            holder.letterTV.setVisibility(View.VISIBLE);
            holder.letterTV.setText(String.valueOf(carInfo.getSortLetters().charAt(0)));  // error: contactInfo.getSortLetters().charAt(0)
        } else {
            holder.letterTV.setVisibility(View.GONE);
        }
        if (DEBUG) {
            holder.nameTV.setText(carInfo.getRawName() + " -> " + carInfo.getPinyinName());
        } else {
            holder.nameTV.setText(carInfo.getRawName());
        }
        holder.nameTV.setTextColor(carInfo.isChoice() ? Color.RED : Color.parseColor("#FF5D5D5D"));
        holder.contentV.setBackgroundColor(carInfo.isChoice() ? Color.parseColor("#ffffffff") : Color.parseColor("#FFF3F3F3"));
        return convertView;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            if (section == getSectionForPosition(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return carInfoList.get(position).getSortLetters().charAt(0);
    }

    public void updateContactInfoList(List<CarInfo> list) {
        carInfoList = list;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView letterTV;
        TextView nameTV;
        View contentV;
    }
}
