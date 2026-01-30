package com.ailianlian.ablecisi.handler;

import android.content.Context;

import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

/**
 * ailianlian
 * com.ailianlian.ablecisi.handler
 * TokenExpiredHandler <br>
 * 处理Token过期的逻辑
 * 用于在收到Token过期的响应时，清除本地登录信息并跳转到登录页面
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 21:14
 */
public class TokenExpiredHandler extends AbstractHandler {
    @Override
    public void handle(Context context, String response, HttpClient.HttpCallback callback) {
        Result<?> result = JsonUtil.fromJson(response, Result.class);
        System.out.println("TokenExpiredHandler: 处理Token过期相关逻辑。\n当下服务器响应内容：" + response);
        if (result != null && result.getCode() == StatusCodeConstant.TOKEN_EXPIRED) {
            // 清除本地登录信息并跳转登录页
            LoginInfoUtil.logout(context);
            // 可选：不再回调业务层
            if (callback != null) {
                callback.onFailure("Token已过期，请重新登录");
            }
        } else {
            super.handle(context, response, callback);
        }
    }
}
