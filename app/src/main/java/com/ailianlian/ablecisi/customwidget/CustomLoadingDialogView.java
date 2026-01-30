package com.ailianlian.ablecisi.customwidget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ailianlian.ablecisi.R;

/**
 * 加载对话框
 * 
 * @author Ablecisi
 * @since 2025/4/18
 */
public class CustomLoadingDialogView extends Dialog {

    private String message = "加载中...";
    private TextView messageView;
    private ProgressBar progressBar;

    public CustomLoadingDialogView(@NonNull Context context) {
        super(context);
    }

    public CustomLoadingDialogView(@NonNull Context context, String message) {
        super(context);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置对话框背景透明
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置对话框宽度为屏幕宽度的80%
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getWindow().setAttributes(params);
        
        // 创建布局
        ConstraintLayout layout = new ConstraintLayout(getContext());
        layout.setId(View.generateViewId());
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundResource(R.drawable.bg_loading_dialog);
        
        // 创建进度条
        progressBar = new ProgressBar(getContext());
        progressBar.setId(View.generateViewId());
        ConstraintLayout.LayoutParams progressParams = new ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.topToTop = layout.getId();
        progressParams.startToStart = layout.getId();
        progressParams.endToEnd = layout.getId();
        progressBar.setLayoutParams(progressParams);
        
        // 创建消息文本
        messageView = new TextView(getContext());
        messageView.setId(View.generateViewId());
        messageView.setText(message);
        messageView.setTextColor(Color.WHITE);
        messageView.setTextSize(14);
        ConstraintLayout.LayoutParams messageParams = new ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        messageParams.topToBottom = progressBar.getId();
        messageParams.startToStart = layout.getId();
        messageParams.endToEnd = layout.getId();
        messageParams.topMargin = 20;
        messageView.setLayoutParams(messageParams);
        
        // 添加视图
        layout.addView(progressBar);
        layout.addView(messageView);
        setContentView(layout);
        
        // 设置对话框不可取消
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    /**
     * 设置加载消息
     *
     * @param message 消息内容
     */
    public void setMessage(String message) {
        this.message = message;
        if (messageView != null) {
            messageView.setText(message);
        }
    }
} 