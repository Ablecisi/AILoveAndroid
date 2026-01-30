package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

/**
 * ailianlian
 * com.ailianlian.ablecisi.repository
 * ProfileRepository <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/8/22
 * 星期五
 * 21:33
 */
public class ProfileRepository extends BaseRepository {
    public ProfileRepository(Context context) {
        super(context);
    }

    // 获取用户信息
    public void loadUserProfile(DataCallback<User> dataCallback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/user/profile", new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Result<User> result = JsonUtil.fromJson(response, new TypeToken<Result<User>>() {
                    }.getType());
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        dataCallback.onSuccess(result.getData());
                    } else {
                        dataCallback.onError(result != null ? result.getMsg() : "获取用户信息失败");
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onNetworkError();
                }
            });
        });
    }
}
