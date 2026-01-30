package com.ailianlian.ablecisi.handler;

import android.content.Context;

import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

/**
 * ailianlian
 * com.ailianlian.ablecisi.handler
 * StatusCodeHandler <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 21:50
 */
public class StatusCodeHandler extends AbstractHandler {
    @Override
    public void handle(Context context, String response, HttpClient.HttpCallback callback) {
        // 这里假设 response 形如 "未知的状态码 403"
        if (response != null && response.contains(String.valueOf(StatusCodeConstant.INVALID_CREDENTIALS))) {
            LoginInfoUtil.logout(context);
            if (callback != null) callback.onFailure("登录已过期，请重新登录");
        } else {
            super.handle(context, response, callback);
        }
    }
}
