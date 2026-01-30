package com.ailianlian.ablecisi.customwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.ailianlian.ablecisi.R;

/**
 * 自定义标题栏
 * 支持返回按钮、标题、右侧操作按钮等功能
 * 
 * @author Ablecisi
 * @since 2025/4/18
 */
public class CustomTitleBarView extends FrameLayout {

    private ConstraintLayout rootLayout;
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivRight;
    private TextView tvRight;
    private View divider;

    private OnBackClickListener backClickListener;
    private OnRightClickListener rightClickListener;

    public CustomTitleBarView(@NonNull Context context) {
        this(context, null);
    }

    public CustomTitleBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTitleBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // 加载布局
        View view = LayoutInflater.from(context).inflate(R.layout.layout_title_bar, this, true);
        
        // 初始化视图
        rootLayout = view.findViewById(R.id.root_layout);
        ivBack  = view.findViewById(R.id.iv_back);
        tvTitle = view.findViewById(R.id.tv_title);
        ivRight = view.findViewById(R.id.iv_right);
        tvRight = view.findViewById(R.id.tv_right);
        divider = view.findViewById(R.id.divider);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> {
            if (backClickListener != null) {
                backClickListener.onBackClick();
            }
        });
        
        ivRight.setOnClickListener(v -> {
            if (rightClickListener != null) {
                rightClickListener.onRightClick();
            }
        });
        
        tvRight.setOnClickListener(v -> {
            if (rightClickListener != null) {
                rightClickListener.onRightClick();
            }
        });
        
        // 读取自定义属性
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TitleBarView);
            
            // 设置标题
            String title = ta.getString(R.styleable.TitleBarView_title);
            if (title != null) {
                tvTitle.setText(title);
            }
            
            // 设置返回按钮是否显示
            boolean showBack = ta.getBoolean(R.styleable.TitleBarView_showBack, true);
            ivBack.setVisibility(showBack ? VISIBLE : GONE);
            
            // 设置右侧图标
            Drawable rightIcon = ta.getDrawable(R.styleable.TitleBarView_rightIcon);
            if (rightIcon != null) {
                ivRight.setImageDrawable(rightIcon);
                ivRight.setVisibility(VISIBLE);
                tvRight.setVisibility(GONE);
            }
            
            // 设置右侧文本
            String rightText = ta.getString(R.styleable.TitleBarView_rightText);
            if (rightText != null) {
                tvRight.setText(rightText);
                tvRight.setVisibility(VISIBLE);
                ivRight.setVisibility(GONE);
            }
            
            // 设置分割线是否显示
            boolean showDivider = ta.getBoolean(R.styleable.TitleBarView_showDivider, true);
            divider.setVisibility(showDivider ? VISIBLE : GONE);
            
            // 设置背景颜色
            int backgroundColor = ta.getColor(R.styleable.TitleBarView_barBackground, 
                    ContextCompat.getColor(context, android.R.color.white));
            rootLayout.setBackgroundColor(backgroundColor);
            
            ta.recycle();
        }
    }

    /**
     * 设置标题
     * @param title 标题文本
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * 设置标题
     * @param titleResId 标题资源ID
     */
    public void setTitle(@StringRes int titleResId) {
        tvTitle.setText(titleResId);
    }

    /**
     * 设置返回按钮是否显示
     * @param show 是否显示
     */
    public void showBack(boolean show) {
        ivBack.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 设置返回按钮图标
     * @param resId 图标资源ID
     */
    public void setBackIcon(@DrawableRes int resId) {
        ivBack.setImageResource(resId);
    }

    /**
     * 设置右侧图标
     * @param resId 图标资源ID
     */
    public void setRightIcon(@DrawableRes int resId) {
        ivRight.setImageResource(resId);
        ivRight.setVisibility(VISIBLE);
        tvRight.setVisibility(GONE);
    }

    /**
     * 设置右侧文本
     * @param text 文本内容
     */
    public void setRightText(String text) {
        tvRight.setText(text);
        tvRight.setVisibility(VISIBLE);
        ivRight.setVisibility(GONE);
    }

    /**
     * 设置右侧文本
     * @param textResId 文本资源ID
     */
    public void setRightText(@StringRes int textResId) {
        tvRight.setText(textResId);
        tvRight.setVisibility(VISIBLE);
        ivRight.setVisibility(GONE);
    }

    /**
     * 设置分割线是否显示
     * @param show 是否显示
     */
    public void showDivider(boolean show) {
        divider.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 设置背景颜色
     * @param color 颜色值
     */
    public void setBarBackgroundColor(int color) {
        rootLayout.setBackgroundColor(color);
    }

    /**
     * 设置返回按钮点击监听器
     * @param listener 监听器
     */
    public void setOnBackClickListener(OnBackClickListener listener) {
        this.backClickListener = listener;
    }

    /**
     * 设置右侧按钮点击监听器
     * @param listener 监听器
     */
    public void setOnRightClickListener(OnRightClickListener listener) {
        this.rightClickListener = listener;
    }

    /**
     * 返回按钮点击监听器接口
     */
    public interface OnBackClickListener {
        void onBackClick();
    }

    /**
     * 右侧按钮点击监听器接口
     */
    public interface OnRightClickListener {
        void onRightClick();
    }
} 