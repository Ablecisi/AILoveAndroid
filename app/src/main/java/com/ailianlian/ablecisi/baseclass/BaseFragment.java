package com.ailianlian.ablecisi.baseclass;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.ailianlian.ablecisi.customwidget.CustomLoadingDialogView;

/**
 * Fragment基类
 * 
 * @author Ablecisi
 * @since 2025/4/18
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {
    
    protected VB binding;
    private CustomLoadingDialogView customLoadingDialogView; // 自定义加载对话框视图
    private boolean isViewCreated = false; // 视图是否已创建
    private boolean isVisibleToUser = false; // Fragment是否对用户可见
    private boolean isDataLoaded = false; // 数据是否已加载

    /**
     * 获取ViewBinding实例
     */
    protected abstract VB getViewBinding(LayoutInflater inflater, ViewGroup container);

    /**
     * 初始化视图
     */
    protected abstract void initView();

    /**
     * 设置监听器
     */
    protected abstract void setListeners();

    /**
     * 懒加载数据，只有当Fragment可见且视图已创建时才会调用
     */
    protected abstract void lazyLoadData();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = getViewBinding(inflater, container);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true; // 标记视图已创建
        initView();
        setListeners();
        setUserVisibleHint(true); // 设置Fragment初始可见性为true
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) { // 处理Fragment可见性变化
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser; // 更新可见性状态
        
        // 处理懒加载
        if (isVisibleToUser && isViewCreated && !isDataLoaded) {
            lazyLoadData();
            isDataLoaded = true;
        }
    }
    
    /** 强制重新加载数据*/
    protected void reloadData() {
        if (isViewCreated) {
            lazyLoadData();
            isDataLoaded = true;
        }
    }
    
    /** 显示加载对话框*/
    protected void showLoading() {
        if (getActivity() == null) return;
        if (customLoadingDialogView == null) {
            customLoadingDialogView = new CustomLoadingDialogView(getActivity());
        }
        if (!customLoadingDialogView.isShowing()) {
            customLoadingDialogView.show();
        }
    }
    
    /** 显示加载对话框（带消息）*/
    protected void showLoading(String message) {
        if (getActivity() == null) return;

        if (customLoadingDialogView == null) {
            customLoadingDialogView = new CustomLoadingDialogView(getActivity(), message);
        } else {
            customLoadingDialogView.setMessage(message);
        }

        if (!customLoadingDialogView.isShowing()) {
            customLoadingDialogView.show();
        }
    }
    
    /** 隐藏加载对话框*/
    protected void hideLoading() {
        if (customLoadingDialogView != null && customLoadingDialogView.isShowing()) {
            customLoadingDialogView.dismiss();
        }
    }
    
    /**
     * 显示Toast消息
     * @param message 消息内容
     */
    protected void showToast(String message) {
        if (getActivity() == null) return;
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示长Toast消息
     * @param message 消息内容
     */
    protected void showLongToast(String message) {
        if (getActivity() == null) return;
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        isDataLoaded = false;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customLoadingDialogView != null) {
            customLoadingDialogView.dismiss();
            customLoadingDialogView = null;
        }
        binding = null;
    }
}