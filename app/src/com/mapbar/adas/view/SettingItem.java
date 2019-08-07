package com.mapbar.adas.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapbar.adas.utils.OBDUtils;
import com.miyuan.obd.R;

import java.util.ArrayList;
import java.util.List;


public class SettingItem extends RelativeLayout implements View.OnClickListener {

    private OnItemClickListener onItemClickListener;

    private TextView titleTV;
    private TextView subTitleTV;
    private TextView unitTV;
    private View iconV;
    private View operateV;
    private TextView contentTV;
    private View subtractV;
    private View subtractIconV;
    private View addV;
    private View addIconV;

    private List<String> content;

    public SettingItem(Context context) {
        this(context, null);
    }

    public SettingItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.setting_item_layout, null);
        titleTV = view.findViewById(R.id.title);
        operateV = view.findViewById(R.id.operate);
        subTitleTV = view.findViewById(R.id.subTitle);
        unitTV = view.findViewById(R.id.unit);
        iconV = view.findViewById(R.id.icon);
        contentTV = view.findViewById(R.id.content);
        subtractV = view.findViewById(R.id.subtract);
        subtractIconV = view.findViewById(R.id.subtract_icon);
        subtractV.setOnClickListener(this);
        addV = view.findViewById(R.id.add);
        addIconV = view.findViewById(R.id.add_icon);
        addV.setOnClickListener(this);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SettingItemView);
        titleTV.setText(array.getString(R.styleable.SettingItemView_title));
        subTitleTV.setText(array.getString(R.styleable.SettingItemView_subTitle));
        unitTV.setText(array.getString(R.styleable.SettingItemView_unit));
        CharSequence[] textArray = array.getTextArray(R.styleable.SettingItemView_content);
        content = new ArrayList<>();
        for (int i = 0; i < textArray.length; i++) {
            String v = String.valueOf(textArray[i]);
            content.add(v);
        }
        if (content.size() > 0) {
            contentTV.setText(String.valueOf(content.get(0)));
        }
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) operateV.getLayoutParams();
        switch (array.getInt(R.styleable.SettingItemView_subTitleType, 0)) {
            case 1:
                layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
                layoutParams.topMargin = OBDUtils.getDimens(getContext(), R.dimen.top_9);
                break;
            case 2:
                layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
                layoutParams.topMargin = OBDUtils.getDimens(getContext(), R.dimen.top_6);
                break;
            default:
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                break;
        }
        operateV.setLayoutParams(layoutParams);
        iconV.setBackgroundResource(array.getResourceId(R.styleable.SettingItemView_icon, R.drawable.light));
        array.recycle();
        this.addView(view);
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateContent(String value) {
        int index = content.indexOf(value);
        if (index < 0 || index >= content.size()) {
            index = 0;
        }
        contentTV.setText(content.get(index));
        if (index == 0) {
            subtractIconV.setBackgroundResource(R.drawable.subtract_dis);
        } else if (index == content.size() - 1) {
            addIconV.setBackgroundResource(R.drawable.add_dis);
        } else {
            subtractIconV.setBackgroundResource(R.drawable.subtract);
            addIconV.setBackgroundResource(R.drawable.add);
        }

    }

    @Override
    public void onClick(View v) {
        if (content.size() <= 0 || onItemClickListener == null) {
            return;
        }
        String value = String.valueOf(contentTV.getText());
        int index = content.indexOf(value);
        switch (v.getId()) {
            case R.id.subtract:
                if (index > 0) {
                    onItemClickListener.onLeftClick(content.get(index - 1));
                }
                break;
            case R.id.add:
                if (index >= 0 && index < content.size() - 1) {
                    onItemClickListener.onRightClick(content.get(index + 1));
                }
                break;
            default:
                break;
        }
    }
}
