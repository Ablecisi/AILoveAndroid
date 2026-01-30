package com.ailianlian.ablecisi.baseclass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView适配器基类
 * 提供通用的数据操作和事件处理功能
 * 
 * @author Ablecisi
 * @since 2025/4/18
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected Context context;
    protected List<T> dataList;
    protected OnItemClickListener<T> onItemClickListener; // 点击事件监听器
    protected OnItemLongClickListener<T> onItemLongClickListener; // 长按事件监听器

    public BaseAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    public BaseAdapter(Context context, List<T> dataList) {
        this.context = context;
        this.dataList = dataList == null ? new ArrayList<>() : dataList;
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    /**
     * 获取指定位置的数据项
     * @param position 位置
     * @return 数据项
     */
    public T getItem(int position) {
        if (position < 0 || position >= dataList.size()) {
            return null;
        }
        return dataList.get(position);
    }

    /**
     * 获取数据列表
     * @return 数据列表
     */
    public List<T> getDataList() {
        return dataList;
    }

    /**
     * 设置数据列表
     * @param dataList 数据列表
     */
    public void setDataList(List<T> dataList) {
        this.dataList = dataList == null ? new ArrayList<>() : dataList;
        notifyDataSetChanged();
    }

    /**
     * 添加数据项
     * @param item 数据项
     */
    public void addItem(T item) {
        if (item == null) return;
        
        dataList.add(item);
        notifyItemInserted(dataList.size() - 1);
    }

    /**
     * 在指定位置添加数据项
     * @param position 位置
     * @param item 数据项
     */
    public void addItem(int position, T item) {
        if (item == null) return;
        if (position < 0 || position > dataList.size()) return;
        
        dataList.add(position, item);
        notifyItemInserted(position);
    }

    /**
     * 添加数据列表
     * @param items 数据列表
     */
    public void addItems(List<T> items) {
        if (items == null || items.isEmpty()) return;
        
        int startPosition = dataList.size();
        dataList.addAll(items);
        notifyItemRangeInserted(startPosition, items.size());
    }

    /**
     * 更新指定位置的数据项
     * @param position 位置
     * @param item 数据项
     */
    public void updateItem(int position, T item) {
        if (item == null) return;
        if (position < 0 || position >= dataList.size()) return;
        
        dataList.set(position, item);
        notifyItemChanged(position);
    }

    /**
     * 移除指定位置的数据项
     * @param position 位置
     */
    public void removeItem(int position) {
        if (position < 0 || position >= dataList.size()) return;
        
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 移除指定数据项
     * @param item 数据项
     */
    public void removeItem(T item) {
        if (item == null) return;
        
        int position = dataList.indexOf(item);
        if (position != -1) {
            dataList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * 清空数据列表
     */
    public void clearItems() {
        dataList.clear();
        notifyDataSetChanged();
    }

    /**
     * 设置项点击监听器
     * @param listener 监听器
     */
    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
    }

    /**
     * 设置项长按监听器
     * @param listener 监听器
     */
    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
    }

    /**
     * 项点击监听器接口
     */
    public interface OnItemClickListener<T> {
        /**
         * 当项被点击时调用
         * @param item 数据项
         * @param position 位置
         */
        void onItemClick(T item, int position);
    }

    /**
     * 项长按监听器接口
     */
    public interface OnItemLongClickListener<T> {
        /**
         * 当项被长按时调用
         * @param item 数据项
         * @param position 位置
         * @return 是否消费长按事件
         */
        boolean onItemLongClick(T item, int position);
    }

    /**
     * 创建ViewHolder
     * @param parent 父视图
     * @param viewType 视图类型
     * @return ViewHolder
     */
    @NonNull
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * 绑定ViewHolder
     * @param holder ViewHolder
     * @param position 位置
     */
    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    /**
     * 创建布局
     * @param parent 父视图
     * @param layoutResId 布局资源ID
     * @return 视图
     */
    protected View inflateView(ViewGroup parent, int layoutResId) {
        return LayoutInflater.from(context).inflate(layoutResId, parent, false);
    }
} 