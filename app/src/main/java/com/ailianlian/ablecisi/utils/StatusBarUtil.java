package com.ailianlian.ablecisi.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

/**
 * 状态栏工具类
 * 
 * @author Ablecisi
 * @since 2025/4/17
 */
public class StatusBarUtil {

    /**
     * 设置状态栏颜色
     *
     * @param activity Activity实例
     * @param colorResId 颜色资源ID
     */
    public static void setStatusBarColor(Activity activity, @ColorRes int colorResId) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, colorResId));
    }

    /**
     * 设置状态栏亮色模式（状态栏字体颜色为深色）
     *
     * @param activity Activity实例
     */
    public static void setStatusBarLight(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;

        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
    }

    /**
     * 设置状态栏暗色模式（状态栏字体颜色为浅色）
     *
     * @param activity Activity实例
     */
    public static void setStatusBarDark(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;

        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    /**
     * 设置透明状态栏
     *
     * @param activity Activity实例
     */
    public static void setTransparentStatusBar(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    /**
     * 获取状态栏高度
     *
     * @param activity Activity实例
     * @return 状态栏高度（单位：像素）
     */
    public static int getStatusBarHeight(Activity activity) {
        if (activity == null) return 0;

        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
} 