package com.ailianlian.ablecisi.utils;

import android.content.Context;
import android.util.Log;

import com.ailianlian.ablecisi.constant.NetWorkPathConstant;
import com.ailianlian.ablecisi.handler.AbstractHandler;
import com.ailianlian.ablecisi.handler.GeneralResponseHandler;
import com.ailianlian.ablecisi.handler.StatusCodeHandler;
import com.ailianlian.ablecisi.handler.TokenExpiredHandler;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ailianlian
 * com.ailianlian.ablecisi.utils
 * HttpClient <br>
 * <h1>请求网络数据的工具类</h1>
 * <hr></hr> 使用说明：
 * 发起请求 直接调用 HttpClient.doGet(context, endpoint, callback) 或 HttpClient.doPost(context, endpoint, body, callback) 即可。 你只需关注业务回调，token 过期等通用逻辑已自动处理。
 * 回调处理 实现 HttpClient.HttpCallback，在 onSuccess 处理业务数据，在 onFailure 处理错误。
 * <hr></hr> 添加中间逻辑（如统一错误码、日志、数据预处理等）：
 * 新建处理器类，继承 AbstractHandler，实现 handle 方法。
 * 在 HttpClient 的 static 块中，将新处理器插入到责任链合适位置。
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/4/19
 * 星期六
 * 14:33
 */

public class HttpClient {
    private static final String TAG = "HttpClient";
    private static final String BASE_URL = NetWorkPathConstant.BASE_URL + "/api";
    private static final int TIMEOUT = 30; // 超时时间，单位秒
    private static final String TOKEN_TAG = "token";
    private static String TOKEN = "";

    // 责任链头部
    private static final AbstractHandler handlerChain;

    // 静态块初始化责任链
    static {
        // 这些处理器可以根据需要增删改顺序
        StatusCodeHandler statusCodeHandler = new StatusCodeHandler();
        TokenExpiredHandler tokenExpiredHandler = new TokenExpiredHandler();
        GeneralResponseHandler generalResponseHandler = new GeneralResponseHandler();
        statusCodeHandler.setNextHandler(tokenExpiredHandler);
        tokenExpiredHandler.setNextHandler(generalResponseHandler);
        handlerChain = statusCodeHandler;
    }

    // 回调接口
    public interface HttpCallback {
        void onSuccess(String response);

        void onFailure(String error);
    }

    // 统一处理响应，所有响应都走责任链
    public static void handleResponse(Context context, String response, HttpCallback callback) {
        handlerChain.handle(context, response, callback);
    }

    // 通过责任链处理成功响应
    private static void postSuccess(Context context, HttpCallback callback, String response) {
        handleResponse(context, response, callback);
    }

    // 失败直接回调
    private static void postFailure(Context context, HttpCallback callback, String error) {
        if (callback != null) {
            callback.onFailure(error);
        }
    }

    /**
     * 发起GET请求
     *
     * @param context  上下文
     * @param endpoint 相对于BASE_URL的端点
     * @param callback 回调接口
     */
    public static void doGet(Context context, String endpoint, HttpCallback callback) {
        TOKEN = LoginInfoUtil.getUserToken(context);
        String url = BASE_URL + endpoint;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader(TOKEN_TAG, TOKEN)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                postFailure(context, callback, "未知的状态码 " + response.code());
                return;
            }
            if (response.body() == null) {
                postFailure(context, callback, "响应体为null");
                return;
            }
            String respStr = response.body().string();
            Log.i(TAG, "获取成功: " + response.code());
            postSuccess(context, callback, respStr);
        } catch (Exception e) {
            Log.e(TAG, "网络请求失败: " + e.getMessage());
            postFailure(context, callback, "网络错误: " + e.getMessage());
        }
    }

    /**
     * 发起POST请求
     * @param context 上下文
     * @param endpoint 相对于BASE_URL的端点
     * @param body 请求体对象，会被序列化为JSON
     * @param callback 回调接口
     * @param <T> 请求体类型
     */
    public static <T> void doPost(Context context, String endpoint, T body, HttpCallback callback) {
        TOKEN = LoginInfoUtil.getUserToken(context);
        String url = BASE_URL + endpoint;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader(TOKEN_TAG, TOKEN)
                .post(RequestBody.create(JsonUtil.toJson(body), MediaType.parse("application/json; charset=utf-8")))
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                postFailure(context, callback, "未知的状态码 " + response.code());
                return;
            }
            if (response.body() == null) {
                postFailure(context, callback, "响应体为空(null)");
                return;
            }
            String respStr = response.body().string();
            Log.i(TAG, "获取成功: " + response.code());
            postSuccess(context, callback, respStr);
        } catch (Exception e) {
            Log.e(TAG, "网络请求失败: " + e.getMessage());
            postFailure(context, callback, "网络错误: " + e.getMessage());
        }

    }
}