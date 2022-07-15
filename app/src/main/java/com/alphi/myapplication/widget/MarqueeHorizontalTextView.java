package com.alphi.myapplication.widget;

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
    private float drawTextX = 0f;// 文本的横坐标
    public boolean isStarting;// 是否开始滚动
    private Paint paint;
    private String text;
    private final long waitTime = 1000; //开始时等待的时间
    private final int scrollTile = 1; //文字的滚动速度

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
        paint = getPaint();
        paint.setColor(getTextColors().getColorForState(getDrawableState(), 0));
        text = getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        textLength = paint.measureText(text);
        isStarting = true;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        this.text = text.toString();
        this.textLength = getPaint().measureText(text.toString());
        drawTextX = 0;
        start();
    }

    public void start() {
        isStarting = true;
        invalidate();
    }

    public void stop() {
        isStarting = false;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        final Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (getHeight() - fontMetrics.bottom - fontMetrics.top) / 2;        //设置基线
        if (textLength <= getWidth()) {
            canvas.drawText(text, 0, baseline, paint);
            return;
        }
        canvas.drawText(text, -drawTextX, baseline, paint);

        if (!isStarting) {
            return;
        }
        if (drawTextX == 0) {
            postDelayed(() -> {
                drawTextX = 1;  // 赋值文本的横坐标为 1
                isStarting = true;
                invalidate();       // 结束onDraw执行
            }, waitTime);
            isStarting = false;
            return;
        }
        drawTextX += scrollTile;
        //判断是否滚动结束
        if (drawTextX > textLength) {
            // 文本开头在屏幕最右边
            drawTextX = -getWidth();
        }
        invalidate();
    }
}
