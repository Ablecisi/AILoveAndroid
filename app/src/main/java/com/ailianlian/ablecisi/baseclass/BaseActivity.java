package com.ailianlian.ablecisi.baseclass;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewbinding.ViewBinding;

import com.ailianlian.ablecisi.customwidget.CustomLoadingDialogView;
import com.ailianlian.ablecisi.utils.StatusBarUtil;

/**
 * Activity基类
 * 
 * @author Ablecisi
 * @since 2025/4/17
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    
    protected VB binding;
    private CustomLoadingDialogView customLoadingDialogView;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean isDark = preferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );
        // 初始化ViewBinding
        binding = getViewBinding();
        setContentView(binding.getRoot());
        initStatusBar();// 初始化状态栏
        initLoadingDialog();// 初始化Loading对话框
        initView();// 初始化视图
        initData();// 初始化数据
        setListeners();// 设置监听器
    }
    
    /** 获取ViewBinding实例*/
    protected abstract VB getViewBinding();
    /** 初始化视图*/
    protected abstract void initView();
    /** 初始化数据*/
    protected abstract void initData();
    /** 设置监听器*/
    protected abstract void setListeners();
    
    /** 初始化状态栏*/
    protected void initStatusBar() {
        StatusBarUtil.setStatusBarLight(this);
        StatusBarUtil.setStatusBarColor(this, android.R.color.white);
    }
    
    /** 初始化Loading对话框*/
    private void initLoadingDialog() {
        customLoadingDialogView = new CustomLoadingDialogView(this);
    }
    
    /** 显示加载对话框*/
    protected void isLoading(Boolean isLoading) {
        if (customLoadingDialogView != null) {
            if (isLoading) {
                customLoadingDialogView.show();
            } else {
                customLoadingDialogView.dismiss();
            }
        }
    }

    /**
     * 显示Toast消息
     * @param message 消息内容
     */
    protected void showToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    protected void showToast(int resId) {
        runOnUiThread(() -> {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * 显示长Toast消息
     * @param message 消息内容
     */
    protected void showLongToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    protected void showLongToast(int resId) {
        runOnUiThread(() -> {
            Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customLoadingDialogView != null) {
            customLoadingDialogView.dismiss();
            customLoadingDialogView = null;
        }
    }
} 