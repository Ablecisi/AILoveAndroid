package com.ailianlian.ablecisi.handler;

import android.content.Context;

import com.ailianlian.ablecisi.utils.HttpClient;

/**
 * ailianlian
 * com.ailianlian.ablecisi.handler
 * AbstractHandler <br>
 * 抽象处理器类，用于定义处理器的基本结构和方法
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 21:13
 */
public abstract class AbstractHandler {
    private AbstractHandler nextHandler;

    public void setNextHandler(AbstractHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handle(Context context, String response, HttpClient.HttpCallback callback) {
        if (nextHandler != null) {
            nextHandler.handle(context, response, callback);
        }
    }
}
