package com.ailianlian.ablecisi.customwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.ailianlian.ablecisi.R;

/**
 * ailianlian
 * com.ailianlian.ablecisi.common.widget
 * RoundedRectImageView <br>
 * 自定义圆角矩形ImageView
 * @author Ablecisi
 * @version 1.0
 * 2025/4/17
 * 星期四
 * 10:40
 */
public class CustomRoundedRectImageView extends AppCompatImageView {

    private float cornerRadius = 20f; // 默认圆角半径
    private Path path; // 用于绘制圆角路径
    private RectF rect; // 用于绘制矩形区域

    public CustomRoundedRectImageView(Context context) {
        super(context);
        init(null);
    }

    public CustomRoundedRectImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomRoundedRectImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        path = new Path();
        rect = new RectF();

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedRectImageView);
            cornerRadius = a.getDimension(R.styleable.RoundedRectImageView_cornerRadius, cornerRadius);
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect.set(0, 0, getWidth(), getHeight());
        path.reset();
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }

    public void setCornerRadius(float radius) {
        cornerRadius = radius;
        invalidate();
    }

    public float getCornerRadius() {
        return cornerRadius;
    }
}
