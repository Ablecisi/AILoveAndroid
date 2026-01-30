package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.pojo.entity.Topic;
import com.ailianlian.ablecisi.pojo.vo.AiCharacterVO;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * ailianlian
 * com.ailianlian.ablecisi.repository
 * HomeRepository <br>
 * 数据仓库类，负责处理首页相关的数据请求
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/15
 * 星期日
 * 23:20
 */
// HomeRepository.java
public class HomeRepository extends BaseRepository {

    public HomeRepository(Context context) {
        super(context);
    }

    // 获取用户兴趣标签
    public void loadUserInterests(DataCallback<List<String>> callback) {
        String userId = LoginInfoUtil.getUserId(getContext());
        if (userId == null || userId.isEmpty()) {
            callback.onError("用户未登录");
            return;
        }
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/user/interests?userId=" + userId, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Result<List<String>> result = JsonUtil.fromJson(response, new TypeToken<Result<List<String>>>() {
                    }.getType());
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        callback.onSuccess(result.getData());
                    } else {
                        callback.onError(result != null ? result.getMsg() : "未知错误");
                    }
                }

                @Override
                public void onFailure(String error) {
                    callback.onError(error);
                }
            });
        });
    }

    // 获取热门AI角色
    public void loadHotCharacters(Long userId, Long typeId, Integer status, String keyword, int page, int size,
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
                            Type type = new com.google.common.reflect.TypeToken<Result<PageResult<AiCharacterVO>>>() {
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

    // 获取精选内容
    public void loadFeaturedContent(DataCallback<Article> callback) {
        // 获取用户兴趣
        List<String> tags = LoginInfoUtil.getUserInterests(getContext());
        getExecutorService().execute(() -> {
            HttpClient.doPost(getContext(), "/article/featured", tags, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Result<Article> result = JsonUtil.fromJson(response, new TypeToken<Result<Article>>() {
                    }.getType());
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        callback.onSuccess(result.getData());
                    } else {
                        callback.onError(result != null ? result.getMsg() : "未知错误");
                    }
                }

                @Override
                public void onFailure(String error) {
                    callback.onError(error);
                }
            });
        });
    }

    // 获取热门话题
    public void loadTopics(DataCallback<List<Topic>> callback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/article/hot", new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Result<List<Article>> result = JsonUtil.fromJson(response, new TypeToken<Result<List<Article>>>() {
                    }.getType());
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        List<Topic> topics = new ArrayList<>();
                        int index = 1; // 用于标识话题的索引
                        for (Article article : result.getData()) {
                            Topic topic = new Topic(article.getId(), article.getTitle(), index, String.valueOf(article.getViewCount()), String.valueOf(article.getComments().size()), String.valueOf(article.getLikeCount()));
                            topics.add(topic);
                            index++;
                        }

                        callback.onSuccess(topics);
                    } else {
                        callback.onError(result != null ? result.getMsg() : "未知错误");
                    }
                }

                @Override
                public void onFailure(String error) {
                    callback.onError(error);
                }
            });
        });
    }

    public void loadCategories(DataCallback<List<String>> cb) {
        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), "/character/types", new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = new TypeToken<Result<List<String>>>() {
                            }.getType();
                            Result<List<String>> r = JsonUtil.fromJson(response, type);
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
}
