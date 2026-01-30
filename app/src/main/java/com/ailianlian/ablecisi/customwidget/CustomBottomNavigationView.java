package com.ailianlian.ablecisi.customwidget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.lang.reflect.Field;

/**
 * 自定义底部导航栏
 * 支持角标显示、自定义选中/未选中状态、动画效果等
 * 
 * @author Ablecisi
 * @since 2024/4/18
 */
public class CustomBottomNavigationView extends FrameLayout {

    private BottomNavigationView navigationView;
    private OnTabSelectedListener tabSelectedListener;

    public CustomBottomNavigationView(@NonNull Context context) {
        this(context, null);
    }

    public CustomBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 创建底部导航视图
        navigationView = new BottomNavigationView(context);
        
        // 设置底部导航视图的外观
        ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCornerSize(24)
                .setTopRightCornerSize(24)
                .build();
        
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        materialShapeDrawable.setFillColor(ColorStateList.valueOf(0xFFFFFFFF)); // 白色背景
        materialShapeDrawable.setElevation(16); // 设置阴影
        
        navigationView.setBackground(materialShapeDrawable);
        
        // 禁用默认的位移动画
        navigationView.setItemHorizontalTranslationEnabled(false);
        
        // 设置选中项监听器
        navigationView.setOnItemSelectedListener(item -> {
            if (tabSelectedListener != null) {
                return tabSelectedListener.onTabSelected(item.getItemId());
            }
            return true;
        });
        
        // 设置重选项监听器
        navigationView.setOnItemReselectedListener(item -> {
            if (tabSelectedListener != null) {
                tabSelectedListener.onTabReselected(item.getItemId());
            }
        });
        
        // 添加到布局中
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        addView(navigationView, params);
    }

    /**
     * 设置导航菜单
     * @param menuResId 菜单资源ID
     */
    public void setMenu(int menuResId) {
        navigationView.inflateMenu(menuResId);
    }

    /**
     * 设置当前选中的项
     * @param itemId 项ID
     */
    public void setSelectedItemId(int itemId) {
        navigationView.setSelectedItemId(itemId);
    }

    /**
     * 获取当前选中的项ID
     * @return 当前选中的项ID
     */
    public int getSelectedItemId() {
        return navigationView.getSelectedItemId();
    }

    /**
     * 设置标签选中监听器
     * @param listener 监听器
     */
    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.tabSelectedListener = listener;
    }

    /**
     * 显示角标
     * @param itemId 项ID
     * @param count 角标数量，0表示隐藏角标
     */
    public void showBadge(int itemId, int count) {
        if (count <= 0) {
            // 隐藏角标
            navigationView.removeBadge(itemId);
            return;
        }
        
        // 显示角标
        navigationView.getOrCreateBadge(itemId).setNumber(count);
    }

    /**
     * 设置项图标
     * @param itemId 项ID
     * @param iconResId 图标资源ID
     */
    public void setItemIcon(int itemId, @DrawableRes int iconResId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        if (item != null) {
            item.setIcon(iconResId);
        }
    }

    /**
     * 设置项图标颜色
     * @param colorStateList 颜色状态列表
     */
    public void setItemIconTintList(@Nullable ColorStateList colorStateList) {
        navigationView.setItemIconTintList(colorStateList);
    }

    /**
     * 设置项文本颜色
     * @param colorStateList 颜色状态列表
     */
    public void setItemTextColor(@Nullable ColorStateList colorStateList) {
        navigationView.setItemTextColor(colorStateList);
    }

    /**
     * 设置项图标大小
     * @param itemId 项ID
     * @param size 图标大小（单位：dp）
     */
    public void setItemIconSize(int itemId, int size) {
        try {
            Field menuViewField = navigationView.getClass().getDeclaredField("menuView");
            menuViewField.setAccessible(true);
            Object menuView = menuViewField.get(navigationView);

            Field itemViewField = menuView.getClass().getDeclaredField("buttons");
            itemViewField.setAccessible(true);
            Object[] itemViews = (Object[]) itemViewField.get(menuView);

            for (Object itemView : itemViews) {
                Field idField = itemView.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                int id = (int) idField.get(itemView);

                if (id == itemId) {
                    Field iconField = itemView.getClass().getDeclaredField("icon");
                    iconField.setAccessible(true);
                    View iconView = (View) iconField.get(itemView);

                    if (iconView != null) {
                        ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                        final int sizeInPixels = (int) (size * getResources().getDisplayMetrics().density);
                        layoutParams.width = sizeInPixels;
                        layoutParams.height = sizeInPixels;
                        iconView.setLayoutParams(layoutParams);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取原始的BottomNavigationView
     * @return BottomNavigationView实例
     */
    public BottomNavigationView getNavigationView() {
        return navigationView;
    }

    /**
     * 标签选中监听器接口
     */
    public interface OnTabSelectedListener {
        /**
         * 当标签被选中时调用
         * @param itemId 项ID
         * @return 是否允许选中
         */
        boolean onTabSelected(int itemId);

        /**
         * 当已选中的标签被再次选中时调用
         * @param itemId 项ID
         */
        void onTabReselected(int itemId);
    }
} 