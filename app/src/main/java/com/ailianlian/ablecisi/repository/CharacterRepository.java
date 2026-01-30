package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.AiCharacterCreateDTO;
import com.ailianlian.ablecisi.pojo.dto.AiCharacterUpdateDTO;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * ailianlian
 * com.ailianlian.ablecisi.repository
 * CharacterRepository <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 20:14
 */
// package: com.ailianlian.ablecisi.repository
public class CharacterRepository extends BaseRepository {

    public CharacterRepository(Context context) {
        super(context);
    }

    public void list(Long userId, Long typeId, Integer status, String keyword, int page, int size,
                     DataCallback<PageResult<AiCharacterVO>> cb) {
        StringBuilder ep = new StringBuilder("/character/list")
                .append("?page=").append(page).append("&size=").append(size);
        if (userId != null) ep.append("&userId=").append(userId);
        if (typeId != null) ep.append("&typeId=").append(typeId);
        if (status != null) ep.append("&status=").append(status);
        if (keyword != null && !keyword.isEmpty()) ep.append("&keyword=").append(keyword);

        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), ep.toString(), new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = new TypeToken<Result<PageResult<AiCharacterVO>>>() {
                            }.getType();
                            Result<PageResult<AiCharacterVO>> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.getCode() == StatusCodeConstant.SUCCESS)
                                cb.onSuccess(r.getData());
                            else cb.onError(r == null ? "解析失败" : r.getMsg());
                        } catch (Exception e) {
                            cb.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        cb.onError(error);
                    }
                })
        );
    }

    public void create(AiCharacterCreateDTO body, DataCallback<Long> cb) {
        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/character/create", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = new TypeToken<Result<Long>>() {
                            }.getType();
                            Result<Long> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.getCode() == StatusCodeConstant.SUCCESS)
                                cb.onSuccess(r.getData());
                            else cb.onError(r == null ? "解析失败" : r.getMsg());
                        } catch (Exception e) {
                            cb.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        cb.onError(error);
                    }
                })
        );
    }

    public void update(long id, AiCharacterUpdateDTO body, DataCallback<Boolean> cb) {
        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/character/update/" + id, body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = new TypeToken<Result<Boolean>>() {
                            }.getType();
                            Result<Boolean> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.getCode() == StatusCodeConstant.SUCCESS)
                                cb.onSuccess(r.getData());
                            else cb.onError(r == null ? "解析失败" : r.getMsg());
                        } catch (Exception e) {
                            cb.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        cb.onError(error);
                    }
                })
        );
    }

    public void detail(long id, DataCallback<AiCharacterVO> cb) {
        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), "/character/" + id, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = new TypeToken<Result<AiCharacterVO>>() {
                            }.getType();
                            Result<AiCharacterVO> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.getCode() == StatusCodeConstant.SUCCESS)
                                cb.onSuccess(r.getData());
                            else cb.onError(r == null ? "解析失败" : r.getMsg());
                        } catch (Exception e) {
                            cb.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        cb.onError(error);
                    }
                })
        );
    }

    public void setOnline(long id, boolean online, DataCallback<Boolean> cb) {
        String ep = "/character/" + id + "/online?online=" + (online ? 1 : 0);
        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), ep, new Object(), new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = new TypeToken<Result<Boolean>>() {
                            }.getType();
                            Result<Boolean> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.getCode() == StatusCodeConstant.SUCCESS)
                                cb.onSuccess(r.getData());
                            else cb.onError(r == null ? "解析失败" : r.getMsg());
                        } catch (Exception e) {
                            cb.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        cb.onError(error);
                    }
                })
        );
    }


    public void getCharacterTypes(BaseRepository.DataCallback<List<String>> cb) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/character/types", new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        Type type = new TypeToken<Result<List<String>>>() {
                        }.getType();
                        Result<List<String>> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS) {
                            cb.onSuccess(r.getData());
                        } else {
                            cb.onError(r == null ? "解析失败" : r.getMsg());
                        }
                    } catch (Exception e) {
                        cb.onError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(String error) {
                    cb.onError(error);
                }
            });
        });
    }
}

