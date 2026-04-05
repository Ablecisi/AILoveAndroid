package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.result.Result;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录用户前台节流调用 {@code POST /api/user/activity/ping}。
 */
public final class UserActivityPing {

    private static final String PREF = "activity_ping";
    private static final String KEY_LAST = "last_ms";
    private static final long MIN_INTERVAL_MS = 60_000L;
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    private UserActivityPing() {
    }

    public static void maybePing(Context context) {
        if (context == null) {
            return;
        }
        String token = LoginInfoUtil.getUserToken(context);
        if (token == null || token.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        long last = sp.getLong(KEY_LAST, 0);
        if (now - last < MIN_INTERVAL_MS) {
            return;
        }
        sp.edit().putLong(KEY_LAST, now).apply();

        String base = com.ailianlian.ablecisi.constant.NetWorkPathConstant.BASE_URL + "/api";
        EXEC.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(base + "/user/activity/ping")
                        .addHeader("token", token)
                        .post(RequestBody.create("", MediaType.parse("application/json; charset=utf-8")))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.body() != null) {
                        String body = response.body().string();
                        Type type = new TypeToken<Result<Object>>() {
                        }.getType();
                        Result<Object> r = JsonUtil.fromJson(body, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS) {
                            // ok
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }
}
