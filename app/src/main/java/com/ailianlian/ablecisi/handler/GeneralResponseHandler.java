package com.ailianlian.ablecisi.handler;

import android.content.Context;

import com.ailianlian.ablecisi.utils.HttpClient;

/**
 * ailianlian
 * com.ailianlian.ablecisi.handler
 * GeneralResponseHandler <br>
 * 统一处理响应的处理器
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 21:15
 */
public class GeneralResponseHandler extends AbstractHandler {
    @Override
    public void handle(Context context, String response, HttpClient.HttpCallback callback) {
        // 正常业务回调
        if (callback != null) {
            callback.onSuccess(response);
        }
    }
}
