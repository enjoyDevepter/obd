package com.mapbar.adas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.obd.R;

/**
 * Created by guomin on 2018/5/29.
 */

public class SensitiveView extends View {

    private Paint paint;

    private TextPaint textPaint;

    private Type type = Type.MEDIUM;

    private int cricle_radius = OBDUtils.getDimens(this.getContext(), R.dimen.sensitive_radius);
    private int sensitive_padding = OBDUtils.getDimens(this.getContext(), R.dimen.sensitive_padding);
    private int sensitive_divider = OBDUtils.getDimens(this.getContext(), R.dimen.sensitive_divider);

    private int singleStrWidth;

    public SensitiveView(Context context) {
        this(context, null);
    }

    public SensitiveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensitiveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setColor(Color.parseColor("#FFA0A0A0"));
        paint.setTextSize(OBDUtils.getDimens(context, R.dimen.sensitive_text_size));
        paint.setAlpha(Paint.ANTI_ALIAS_FLAG);

        textPaint = new TextPaint();
        textPaint.setColor(Color.parseColor("#FFA0A0A0"));
        textPaint.setTextSize(OBDUtils.getDimens(context, R.dimen.sensitive_text_size));
        textPaint.setAlpha(Paint.ANTI_ALIAS_FLAG);
        singleStrWidth = (int) textPaint.measureText("低");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        // 绘制连接线
        Path path = new Path();
        paint.setColor(Color.parseColor("#FFDCDCDC"));
        path.moveTo(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius / 2);
        path.lineTo(width - sensitive_padding - cricle_radius, sensitive_padding + cricle_radius / 2);
        path.lineTo(width - sensitive_padding - cricle_radius, sensitive_padding + cricle_radius / 2 + cricle_radius);
        path.lineTo(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius / 2 + cricle_radius);
        canvas.drawPath(path, paint);

        paint.setColor(Color.parseColor("#FFA0A0A0"));

        path = new Path();
        paint.setColor(Color.parseColor("#FFDCDCDC"));
        path.moveTo(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius / 2);
        switch (type) {
            case MEDIUM:
                path.lineTo(width / 2, sensitive_padding + cricle_radius / 2);
                path.lineTo(width / 2, sensitive_padding + cricle_radius / 2 + cricle_radius);
                path.lineTo(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius / 2 + cricle_radius);
                canvas.drawCircle(width - sensitive_padding - cricle_radius, sensitive_padding + cricle_radius, cricle_radius, paint);

                paint.setColor(Color.parseColor("#FFA0A0A0"));
                canvas.drawCircle(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius, cricle_radius, paint);
                canvas.drawCircle(width / 2, sensitive_padding + cricle_radius, cricle_radius, paint);
                paint.setColor(Color.parseColor("#FFDCDCDC"));

                break;
            case Hight:
                path.lineTo(width - sensitive_padding - cricle_radius, sensitive_padding + cricle_radius / 2);
                path.lineTo(width - sensitive_padding - cricle_radius, sensitive_padding + cricle_radius / 2 + cricle_radius);
                path.lineTo(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius / 2 + cricle_radius);

                paint.setColor(Color.parseColor("#FFA0A0A0"));
                canvas.drawCircle(sensitive_padding + cricle_radius, sensitive_padding + cricle_radius, cricle_radius, paint);
                canvas.drawCircle(width / 2, sensitive_padding + cricle_radius, cricle_radius, paint);
                canvas.drawCircle(width - sensitive_padding - cricle_radius, sensitive_padding + cricle_radius, cricle_radius, paint);
                paint.setColor(Color.parseColor("#FFDCDCDC"));
                break;
        }
        paint.setColor(Color.parseColor("#FFA0A0A0"));
        canvas.drawPath(path, paint);

        // 绘制文字
        paint.setColor(Color.parseColor("#FF3D424C"));
        canvas.drawText("低", sensitive_padding + cricle_radius - singleStrWidth / 2, sensitive_padding + 2 * cricle_radius + sensitive_divider + textPaint.getFontMetrics().descent, paint);
        canvas.drawText("默认", width / 2 - singleStrWidth, sensitive_padding + 2 * cricle_radius + sensitive_divider + textPaint.getFontMetrics().descent, paint);
        canvas.drawText("高", width - sensitive_padding - cricle_radius - singleStrWidth / 2, sensitive_padding + 2 * cricle_radius + sensitive_divider + textPaint.getFontMetrics().descent, paint);
    }

    public void setType(Type type) {
        this.type = type;
        invalidate();
    }

    enum Type {
        LOW,
        MEDIUM,
        Hight,

    }
}
