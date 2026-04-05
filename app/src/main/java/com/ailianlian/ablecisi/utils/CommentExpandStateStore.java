package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 本地持久化「已展开过楼中楼」的根评论 id（按文章维度），减少离开页面前的写入频率由 Activity 防抖调度。
 */
public final class CommentExpandStateStore {

    private static final String PREF = "comment_expand_state";
    private static final String KEY_PREFIX = "roots_";

    private CommentExpandStateStore() {
    }

    private static SharedPreferences sp(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void saveExpandedRootIds(Context ctx, String articleId, Set<Long> rootIds) {
        if (ctx == null || TextUtils.isEmpty(articleId)) {
            return;
        }
        if (rootIds == null || rootIds.isEmpty()) {
            sp(ctx).edit().remove(KEY_PREFIX + articleId).apply();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : rootIds) {
            if (id == null || id <= 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        sp(ctx).edit().putString(KEY_PREFIX + articleId, sb.toString()).apply();
    }

    public static Set<Long> loadExpandedRootIds(Context ctx, String articleId) {
        if (ctx == null || TextUtils.isEmpty(articleId)) {
            return Collections.emptySet();
        }
        String raw = sp(ctx).getString(KEY_PREFIX + articleId, "");
        if (raw == null || raw.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> out = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            if (part.isEmpty()) {
                continue;
            }
            try {
                out.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }
}
