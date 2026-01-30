package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.LoginTypeConstant;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.UserDTO;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 登录数据仓库
 * 负责处理登录相关的数据操作
 */
public class LoginRepository extends BaseRepository {
    public LoginRepository(Context context) {
        super(context);
    }

    /**
     * 登录方法
     *
     * @param username   用户名
     * @param password   密码
     * @param rememberMe 是否记住我
     * @param callback   登录回调
     */
    public void login(String username, String password, boolean rememberMe, DataCallback<User> callback) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setPassword(password);
        getExecutorService()
                .execute(() -> HttpClient.doPost(getContext(), "/user/login", userDTO, new HttpClient.HttpCallback() {
                            @Override
                            public void onSuccess(String response) {
                                // 解析响应数据
                                if (response == null || response.isEmpty()) {
                                    callback.onError("登录失败，响应数据为空");
                                } else {
                                    // 登录成功
                                    // 解析响应数据（假设返回的response包含token和userId）
                                    Type type = new TypeToken<Result<User>>() {
                                    }.getType();
                                    Result<User> result = JsonUtil.fromJson(response, type);
                                    if (result == null || result.getCode() != StatusCodeConstant.SUCCESS) {
                                        callback.onError(result != null ? result.getMsg() : "登录失败，未知错误");
                                        return;
                                    }
                                    User user = result.getData(); // 因为Result<User>中data是User类型
                                    String token = user.getToken();
                                    String userId = user.getId();
                                    String name = user.getName();
                                    String userName = user.getUsername();
                                    String avatarUrl = user.getAvatarUrl();
                                    // 如果需要记住登录状态，保存到本地
                                    if (rememberMe) {
                                        LoginInfoUtil.saveLoginInfo(getContext(), LoginTypeConstant.LOGIN_TYPE_USER, userId, name, token, userName, avatarUrl);
                                    }
                                    callback.onSuccess(new User(userId, token));
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                // 网络请求失败
                                callback.onNetworkError();
                            }
                        })
                );

    }


    /**
     * 退出登录
     */
    public void logout() {
        LoginInfoUtil.clearLoginInfo(getContext());
    }

    public boolean isLoggedIn() {
        return LoginInfoUtil.isLoggedIn(getContext());
    }

    public String getCurrentUserId() {
        return LoginInfoUtil.getUserId(getContext());
    }

    public String getUserName() {
        return LoginInfoUtil.getName(getContext());
    }
}