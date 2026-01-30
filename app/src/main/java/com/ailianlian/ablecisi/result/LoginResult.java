package com.ailianlian.ablecisi.result;

/**
 * 登录结果模型类
 */
public class LoginResult {
    private final boolean success;
    private String message;
    private String token;
    private String userId;

    // 成功结果构造函数
    public LoginResult(boolean success, String token, String userId) {
        this.success = success;
        this.token = token;
        this.userId = userId;
    }

    // 失败结果构造函数
    public LoginResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 静态工厂方法 - 创建成功结果
    public static LoginResult success(String token, String userId) {
        return new LoginResult(true, token, userId);
    }

    // 静态工厂方法 - 创建失败结果
    public static LoginResult error(String errorMessage) {
        return new LoginResult(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }
} 