package com.miyuan.obd.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.miyuan.obd.R;
import com.miyuan.obd.utils.DecFormatUtil;


public class TextViewFontLcdEx extends TextView {
    // 创建一个字体缓存，使用LRU缓存策略
    private static final LruCache<String, Typeface> typefaceCache = new LruCache<String, Typeface>(
            6);

    private ForegroundColorSpan span = new ForegroundColorSpan(getResources()
            .getColor(R.color.numcol));

    public TextViewFontLcdEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.Typeface, 0, 0);
        try {
            // 取得自定义Button的typeface属性
            String typefaceName = ta.getString(R.styleable.Typeface_typeface);
            if (!isInEditMode() && !TextUtils.isEmpty(typefaceName)) {
                Typeface typeface = typefaceCache.get(typefaceName);

                if (typeface == null) {
                    typeface = Typeface.createFromAsset(context.getAssets(),
                            String.format("fonts/%s", typefaceName));
                    typefaceCache.put(typefaceName, typeface);
                }

                setTypeface(typeface);
            }
        } finally {
            ta.recycle();
        }
    }

    public TextViewFontLcdEx(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.Typeface, 0, 0);
        try {
            // 取得自定义Button的typeface属性
            String typefaceName = ta.getString(R.styleable.Typeface_typeface);
            if (!isInEditMode() && !TextUtils.isEmpty(typefaceName)) {
                Typeface typeface = typefaceCache.get(typefaceName);

                if (typeface == null) {
                    typeface = Typeface.createFromAsset(context.getAssets(),
                            String.format("fonts/%s", typefaceName));
                    typefaceCache.put(typefaceName, typeface);
                }

                setTypeface(typeface);
            }
        } finally {
            ta.recycle();
        }
    }

    public TextViewFontLcdEx(Context context) {
        super(context);
    }

    /**
     * 设置速度 格式要求:000 即百位及十位数如有必要,补暗格的8
     *
     * @param speed
     */
    public void setTextFormat000(int speed) {
        String output = DecFormatUtil.format000(speed);
        output += " ";
        if (output.startsWith("00")) {
            output = output.replaceFirst("00", "88");
            SpannableStringBuilder style = new SpannableStringBuilder();
            style.append(output);
            style.setSpan(span, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.setText(style);
        } else if (output.startsWith("0")) {
            output = output.replaceFirst("0", "8");
            SpannableStringBuilder style = new SpannableStringBuilder();
            style.append(output);
            style.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.setText(style);
        } else {
            this.setText(output);
        }
    }

    /**
     * 设置瞬时油耗,油资 格式要求:00.0 即保留1位小数(4舍5入),十位数如有必要,补暗格的8
     *
     * @param gasConsum
     */
    public void setTextFormat00dot0(float gasConsum) {
        String output = DecFormatUtil.format00dot1(gasConsum);
        output += " ";
        if (output.startsWith("0")) {
            output = output.replaceFirst("0", "8");
            SpannableStringBuilder style = new SpannableStringBuilder();
            style.append(output);
            style.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.setText(style);
        } else {
            this.setText(output);
        }
    }


}
