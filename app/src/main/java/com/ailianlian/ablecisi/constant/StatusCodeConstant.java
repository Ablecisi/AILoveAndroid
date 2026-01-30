package com.ailianlian.ablecisi.constant;

/**
 * ailianlian
 * com.ailianlian.ablecisi.constant
 * LoginStatusCodeConstant <br>
 * 登录状态码常量类
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/15
 * 星期日
 * 18:07
 */
public class StatusCodeConstant {
    // 成功
    public static final int SUCCESS = 1;
    // 默认失败
    public static final int FAILURE = 0;

    // 客户端错误 4xx
    public static final int BAD_REQUEST = 400;               // 请求参数错误
    public static final int UNAUTHORIZED = 401;              // 未认证
    public static final int FORBIDDEN = 403;                 // 权限不足
    public static final int NOT_FOUND = 404;                 // 资源不存在
    public static final int METHOD_NOT_ALLOWED = 405;        // 方法不允许
    public static final int CONFLICT = 409;                  // 资源冲突
    public static final int UNPROCESSABLE_ENTITY = 422;      // 参数校验失败
    public static final int TOO_MANY_REQUESTS = 429;         // 请求过多

    // 服务端错误 5xx
    public static final int INTERNAL_SERVER_ERROR = 500;     // 服务器内部错误
    public static final int BAD_GATEWAY = 502;               // 网关错误
    public static final int SERVICE_UNAVAILABLE = 503;       // 服务不可用
    public static final int GATEWAY_TIMEOUT = 504;           // 网关超时

    // 业务错误码（自定义）
    public static final int INVALID_CREDENTIALS = 1001;      // 凭证无效
    public static final int TOKEN_EXPIRED = 1002;            // Token过期
    public static final int USER_NOT_EXIST = 1003;           // 用户不存在
    public static final int PASSWORD_ERROR = 1004;           // 密码错误
    public static final int ACCOUNT_LOCKED = 1005;           // 账户被锁定
    public static final int EMAIL_ALREADY_EXISTS = 1006;     // 邮箱已存在
    public static final int PHONE_ALREADY_EXISTS = 1007;     // 手机号已存在
    public static final int VERIFICATION_CODE_ERROR = 1008;  // 验证码错误
    public static final int OPERATION_NOT_ALLOWED = 1009;    // 操作不被允许
    public static final int RESOURCE_NOT_FOUND = 1010;       // 资源未找到

    // 其他错误码
    public static final int NETWORK_ERROR = 500;             // 网络错误
    public static final int SERVER_ERROR = 500;              // 服务器错误
    public static final int SQL_ERROR = 600;                 // sql异常
    public static final int FILE_UPLOAD_ERROR = 700;         // 文件上传异常
    public static final int FILE_DOWNLOAD_ERROR = 701;       // 文件下载异常
    public static final int THIRD_PARTY_SERVICE_ERROR = 800; // 第三方服务异常
    public static final int BUSINESS_ERROR = 900;
}
