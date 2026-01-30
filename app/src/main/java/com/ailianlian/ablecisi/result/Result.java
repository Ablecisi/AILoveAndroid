package com.ailianlian.ablecisi.result;

import java.io.Serializable;

/**
 * ailianlian
 * com.ailianlian.ablecisi.result
 * Result <br>
 * 请求结果的封装类
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/4/19
 * 星期六
 * 14:36
 */
public class Result<T> implements Serializable {
    public int code; // 响应码 200表示成功, 404表示未找到, 500表示服务器错误
    public String msg; // 响应消息
    public T data; // 响应数据

    public static <T> Result<T> success(String msg, T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.msg = msg;
        result.code = 200;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 404;
        return result;
    }

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
