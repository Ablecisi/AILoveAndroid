package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ailianlian.ablecisi.pojo.entity.BrowseHistoryEntry;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 本地浏览历史（文章、帖子），最多保留若干条，按最近访问在前。
 */
public final class BrowseHistoryStore {

    private static final String PREF = "browse_history_pref";
    private static final String KEY = "entries";
    private static final int MAX = 80;

    private BrowseHistoryStore() {
    }

    public static void addArticle(Context ctx, String articleId, String title) {
        add(ctx, BrowseHistoryEntry.KIND_ARTICLE, articleId, title != null ? title : "文章");
    }

    public static void addPost(Context ctx, String postId, String preview) {
        add(ctx, BrowseHistoryEntry.KIND_POST, postId, preview != null && !preview.isEmpty() ? preview : "帖子");
    }

    private static void add(Context ctx, String kind, String id, String title) {
        if (ctx == null || id == null || id.isEmpty()) {
            return;
        }
        SharedPreferences sp = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        List<BrowseHistoryEntry> list = loadList(sp);
        Iterator<BrowseHistoryEntry> it = list.iterator();
        while (it.hasNext()) {
            BrowseHistoryEntry e = it.next();
            if (kind.equals(e.kind) && id.equals(e.id)) {
                it.remove();
            }
        }
        list.add(0, new BrowseHistoryEntry(kind, id, title, System.currentTimeMillis()));
        while (list.size() > MAX) {
            list.remove(list.size() - 1);
        }
        sp.edit().putString(KEY, JsonUtil.toJson(list)).apply();
    }

    public static List<BrowseHistoryEntry> getAll(Context ctx) {
        SharedPreferences sp = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return Collections.unmodifiableList(loadList(sp));
    }

    private static List<BrowseHistoryEntry> loadList(SharedPreferences sp) {
        String json = sp.getString(KEY, "[]");
        List<BrowseHistoryEntry> list = JsonUtil.fromJson(json, new TypeToken<List<BrowseHistoryEntry>>() {
        }.getType());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }
}
