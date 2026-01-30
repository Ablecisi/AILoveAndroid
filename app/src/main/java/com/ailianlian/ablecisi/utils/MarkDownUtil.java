package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.widget.TextView;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

/**
 * ailianlian
 * com.ailianlian.ablecisi.utils
 * MarkDownUtil <br>
 * Markdown工具类
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 13:55
 */
public class MarkDownUtil {

    private MarkDownUtil() {
        // 私有构造函数，防止实例化
    }

    // 获取Markwon实例
    public static void setMarkdown(Context context, TextView textView, String markdownText) {
        if (textView == null || markdownText == null || markdownText.isEmpty()) {
            return;
        }
        Markwon markwon = Markwon.builder(context)
                .usePlugin(TablePlugin.create(context))
                .usePlugin(ImagesPlugin.create())
                .usePlugin(GlideImagesPlugin.create(context))
                .build();
        // 可以在这里配置Markwon的样式或插件
        markwon.setMarkdown(textView, markdownText);
    }

    /**
     * 将Markdown文本转换为HTML
     *
     * @param markdownText Markdown文本
     * @return 转换后的HTML文本
     */
    public static String markdownToHtml(String markdownText, Context context) {
        Markwon markwon = Markwon.create(context);
        if (markdownText == null || markdownText.isEmpty()) {
            return "";
        }

        // 使用第三方库或自定义方法将Markdown转换为HTML
        // 这里可以使用如CommonMark等库进行转换
        return "<html><body>" + markdownText + "</body></html>"; // 简单示例，实际应使用Markdown解析库
    }

    /**
     * 将Markdown文本转换为Spannable
     *
     * @param markdownText Markdown文本
     * @return 转换后的Spannable对象
     */
    public static CharSequence markdownToSpannable(String markdownText, Context context) {
        Markwon markwon = Markwon.create(context);
        if (markdownText == null || markdownText.isEmpty()) {
            return "";
        }

        // 使用Markwon将Markdown转换为Spannable
        return markwon.toMarkdown(markdownText);
    }
}
