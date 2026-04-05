package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存 {@code GET /api/app/bootstrap} 返回的键值对（均为字符串）。
 */
public final class AppBootstrapCache {

    private static final String PREF = "app_bootstrap";
    private static final String KEY_JSON = "payload_json";

    private AppBootstrapCache() {
    }

    public static void saveFromJson(Context context, String jsonObjectString) {
        if (context == null || jsonObjectString == null) {
            return;
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_JSON, jsonObjectString)
                .apply();
    }

    /** 解析 {@code Result&lt;Map&gt;} 整段 JSON，仅缓存 {@code data} 对象 */
    public static void saveFromBootstrapResponse(Context context, String fullResponse) {
        if (context == null || fullResponse == null) {
            return;
        }
        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(fullResponse).getAsJsonObject();
            if (root.has("data") && root.get("data").isJsonObject()) {
                saveFromJson(context, root.getAsJsonObject("data").toString());
            }
        } catch (Exception ignored) {
        }
    }

    public static Map<String, String> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_JSON, null);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(raw).getAsJsonObject();
            Map<String, String> m = new HashMap<>();
            for (String k : o.keySet()) {
                if (o.get(k).isJsonPrimitive()) {
                    m.put(k, o.get(k).getAsString());
                }
            }
            return m;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
