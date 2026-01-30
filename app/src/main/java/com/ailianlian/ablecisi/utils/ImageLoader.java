package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * 图片加载工具类
 * 基于Glide封装，提供统一的图片加载接口
 * @author Ablecisi
 * @since 2025/4/17
 */
public class ImageLoader {

    /**
     * 加载图片到ImageView
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     */
    public static void load(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    /**
     * 加载图片到ImageView，带占位图
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     * @param placeholderResId 占位图资源ID
     */
    public static void load(Context context, String url, ImageView imageView, @DrawableRes int placeholderResId) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholderResId)
                .into(imageView);
    }

    /**
     * 加载图片到ImageView，带占位图和错误图
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     * @param placeholderResId 占位图资源ID
     * @param errorResId 错误图资源ID
     */
    public static void load(Context context, String url, ImageView imageView, 
                           @DrawableRes int placeholderResId, @DrawableRes int errorResId) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholderResId)
                .error(errorResId)
                .into(imageView);
    }

    /**
     * 加载圆形图片
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     */
    public static void loadCircle(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);
    }

    /**
     * 加载圆形图片，带占位图
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     * @param placeholderResId 占位图资源ID
     */
    public static void loadCircle(Context context, String url, ImageView imageView, @DrawableRes int placeholderResId) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholderResId)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);
    }

    /**
     * 加载圆角图片
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     * @param radius 圆角半径，单位dp
     */
    public static void loadRoundedCorners(Context context, String url, ImageView imageView, int radius) {
        // 将dp转为px
        int px = (int) (radius * context.getResources().getDisplayMetrics().density + 0.5f);
        
        Glide.with(context)
                .load(url)
                .transform(new CenterCrop(), new RoundedCorners(px))
                .into(imageView);
    }

    /**
     * 加载圆角图片，带占位图
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     * @param radius 圆角半径，单位dp
     * @param placeholderResId 占位图资源ID
     */
    public static void loadRoundedCorners(Context context, String url, ImageView imageView, 
                                         int radius, @DrawableRes int placeholderResId) {
        // 将dp转为px
        int px = (int) (radius * context.getResources().getDisplayMetrics().density + 0.5f);
        
        Glide.with(context)
                .load(url)
                .placeholder(placeholderResId)
                .transform(new CenterCrop(), new RoundedCorners(px))
                .into(imageView);
    }

    /**
     * 加载图片，带加载监听器
     * @param context 上下文
     * @param url 图片URL
     * @param imageView 目标ImageView
     * @param listener 加载监听器
     */
    public static void load(Context context, String url, ImageView imageView, OnImageLoadListener listener) {
        Glide.with(context)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                               Target<Drawable> target, boolean isFirstResource) {
                        if (listener != null) {
                            listener.onLoadFailed(e);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, 
                                                  Target<Drawable> target, DataSource dataSource, 
                                                  boolean isFirstResource) {
                        if (listener != null) {
                            listener.onLoadSuccess(resource);
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * 预加载图片
     * @param context 上下文
     * @param url 图片URL
     */
    public static void preload(Context context, String url) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .preload();
    }

    /**
     * 清除内存缓存
     * @param context 上下文
     */
    public static void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * 清除磁盘缓存
     * @param context 上下文
     */
    public static void clearDiskCache(final Context context) {
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }

    /**
     * 图片加载监听器接口
     */
    public interface OnImageLoadListener {
        /**
         * 加载成功回调
         * @param drawable 加载的Drawable
         */
        void onLoadSuccess(Drawable drawable);

        /**
         * 加载失败回调
         * @param e 异常
         */
        void onLoadFailed(GlideException e);
    }
} 