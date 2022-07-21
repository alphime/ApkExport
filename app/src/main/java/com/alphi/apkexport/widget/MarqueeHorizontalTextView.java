package com.alphi.apkexport.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * IDEA 2022/02/07
 */

@SuppressLint("AppCompatCustomView")
public class MarqueeHorizontalTextView extends TextView {
    private float textLength = 0f;
    // 文本的负横坐标
    private float drawTextX = 0f;
    // 滚动布尔值
    public boolean isScrolling;
    private Paint paint;
    private String text;
    // 停留时间
    private final int delayTime = 1000;
    // 文字的滚动速度
    private final int scrollSpeed = 1;

    public MarqueeHorizontalTextView(Context context) {
        super(context);
        initView();
    }

    public MarqueeHorizontalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MarqueeHorizontalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        // 获取文本绘画属性
        paint = getPaint();
        // 文本颜色
        paint.setColor(getTextColors().getColorForState(getDrawableState(), 0));
        text = getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        // 获取文字的宽度
        textLength = paint.measureText(text);
        isScrolling = true;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        this.text = text.toString();
        this.textLength = getPaint().measureText(text.toString());
        drawTextX = 0;
        start();
    }

    /**
     * 启用跑马灯
     */
    public void start() {
        isScrolling = true;
        invalidate();
    }

    /**
     * 停用跑马灯
     */
    public void stop() {
        isScrolling = false;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 获取字体度量
        final Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        //计算基线
        int baseline = (getHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
        if (textLength <= getWidth()) {
            canvas.drawText(text, 0, baseline, paint);
            return;
        }
        canvas.drawText(text, -drawTextX, baseline, paint);

        if (!isScrolling) {
            return;
        }
        if (drawTextX == 0) {
            // 当回到原点时，暂停一小会
            postDelayed(() -> {
                // 赋值文本的横坐标为 -1
                drawTextX = 1;
                isScrolling = true;
                // 标记使原始图无效，此时呢，视图就要重绘
                invalidate();
            }, delayTime);
            isScrolling = false;
            return;
        }
        drawTextX += scrollSpeed;
        //判断是否滚动结束
        if (drawTextX > textLength) {
            // 文本开头在屏幕最右边
            drawTextX = -getWidth();
        }
        // 重绘视图
        invalidate();
    }
}
