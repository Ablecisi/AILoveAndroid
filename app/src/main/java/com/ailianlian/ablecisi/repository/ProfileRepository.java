package com.ailianlian.ablecisi.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.UserPasswordChangeBodyDTO;
import com.ailianlian.ablecisi.pojo.dto.UserProfileUpdateDTO;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.pojo.entity.UploadImageUrlData;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

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

    private static final int MAX_AVATAR_BYTES = 5 * 1024 * 1024;

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

    public void changePassword(String oldPassword, String newPassword, DataCallback<Void> callback) {
        UserPasswordChangeBodyDTO body = new UserPasswordChangeBodyDTO();
        body.oldPassword = oldPassword;
        body.newPassword = newPassword;
        getExecutorService().execute(() ->
                HttpClient.doPut(getContext(), "/user/password", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Result<Void> result = JsonUtil.fromJson(response, new TypeToken<Result<Void>>() {
                        }.getType());
                        if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(result != null ? result.getMsg() : "修改失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onNetworkError();
                    }
                }));
    }

    public void updateProfile(UserProfileUpdateDTO dto, DataCallback<User> callback) {
        getExecutorService().execute(() ->
                HttpClient.doPut(getContext(), "/user/profile", dto, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Result<User> result = JsonUtil.fromJson(response, new TypeToken<Result<User>>() {
                        }.getType());
                        if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                            callback.onSuccess(result.getData());
                        } else {
                            callback.onError(result != null ? result.getMsg() : "更新失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onNetworkError();
                    }
                }));
    }

    /**
     * 从相册 Uri 读图并上传到 {@code POST /user/uploads/image}，成功回调公网 URL。
     */
    public void uploadAvatarImage(Uri uri, ContentResolver cr, DataCallback<String> callback) {
        getExecutorService().execute(() -> {
            try (InputStream in = cr.openInputStream(uri)) {
                if (in == null) {
                    callback.onError("无法读取图片");
                    return;
                }
                byte[] bytes = readLimited(in, MAX_AVATAR_BYTES);
                if (bytes == null) {
                    callback.onError("图片过大（最大 5MB）");
                    return;
                }
                String mime = cr.getType(uri);
                if (mime == null || mime.isEmpty()) {
                    mime = "image/jpeg";
                }
                String fileName = fileNameForMime(mime);
                HttpClient.doMultipartImageUpload(getContext(), "/user/uploads/image", bytes, fileName, mime,
                        new HttpClient.HttpCallback() {
                            @Override
                            public void onSuccess(String response) {
                                Result<UploadImageUrlData> result = JsonUtil.fromJson(response,
                                        new TypeToken<Result<UploadImageUrlData>>() {
                                        }.getType());
                                if (result != null && result.getCode() == StatusCodeConstant.SUCCESS
                                        && result.getData() != null && result.getData().url != null
                                        && !result.getData().url.isEmpty()) {
                                    callback.onSuccess(result.getData().url);
                                } else {
                                    callback.onError(result != null ? result.getMsg() : "上传失败");
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                callback.onNetworkError();
                            }
                        });
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "读取图片失败");
            }
        });
    }

    private static String fileNameForMime(String mime) {
        String m = mime.toLowerCase(Locale.ROOT);
        if ("image/png".equals(m)) {
            return "avatar.png";
        }
        if ("image/gif".equals(m)) {
            return "avatar.gif";
        }
        if ("image/webp".equals(m)) {
            return "avatar.webp";
        }
        return "avatar.jpg";
    }

    /** 读取不超过 max 字节；超过返回 null */
    private static byte[] readLimited(InputStream in, int max) throws java.io.IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(Math.min(8192, max));
        byte[] chunk = new byte[8192];
        int total = 0;
        int n;
        while ((n = in.read(chunk)) != -1) {
            total += n;
            if (total > max) {
                return null;
            }
            buf.write(chunk, 0, n);
        }
        return buf.toByteArray();
    }
}
