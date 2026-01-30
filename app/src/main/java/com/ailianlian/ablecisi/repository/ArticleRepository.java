package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.UserFollowDTO;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * ailianlian
 * com.ailianlian.ablecisi.repository
 * ArticleRepository <br>
 * 文章仓库
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 09:12
 */
public class ArticleRepository extends BaseRepository {

    public ArticleRepository(Context context) {
        super(context);
    }


    /**
     * 加载文章详情
     *
     * @param articleId    文章ID
     * @param dataCallback 数据回调
     */
    public void loadArticle(String articleId, DataCallback<Article> dataCallback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/article/byId?articleId=" + articleId, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<Article>>() {
                    }.getType();
                    Result<Article> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        Article article = result.getData();
                        dataCallback.onSuccess(article);
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });
    }

    /**
     * 关注或取消关注作者
     *
     * @param userId      用户ID
     * @param authorId    作者ID
     * @param isFollowing 是否关注
     * @param callback    数据回调
     */
    public void followAuthor(String userId, String authorId, boolean isFollowing, DataCallback<Boolean> callback) {
        UserFollowDTO userFollowDTO = new UserFollowDTO();
        userFollowDTO.setUserId(userId);
        userFollowDTO.setAuthorId(authorId);
        userFollowDTO.setFollowing(isFollowing);
        getExecutorService().execute(() -> {
            HttpClient.doPost(getContext(), "/user/follow", userFollowDTO, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<Boolean>>() {
                    }.getType();
                    Result<Boolean> result = JsonUtil.fromJson(response, type);
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

    /**
     * 加载关注状态
     *
     * @param userId       用户ID
     * @param authorId     作者ID
     * @param dataCallback 数据回调
     */
    public void loadFollowStatus(String userId, String authorId, DataCallback<Boolean> dataCallback) {
        getExecutorService().execute(() -> {
            // 在后端方法中参数应该这样写 // @RequestParam("userId") String userId, @RequestParam("authorId") String authorId
            HttpClient.doGet(getContext(), "/user/follow?userId=" + userId + "&authorId=" + authorId, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<Boolean>>() {
                    }.getType();
                    Result<Boolean> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        dataCallback.onSuccess(result.getData());
                    } else {
                        dataCallback.onError(result != null ? result.getMsg() : "未知错误");
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });
    }

    /**
     * 加载相关文章
     *
     * @param articleId    文章ID
     * @param dataCallback 数据回调
     */
    public void loadRelatedArticles(String articleId, DataCallback<List<Article>> dataCallback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/article/related?articleId=" + articleId, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<List<Article>>>() {
                    }.getType();
                    Result<List<Article>> result = JsonUtil.fromJson(response, type);
                    if (result != null && result.getCode() == StatusCodeConstant.SUCCESS) {
                        dataCallback.onSuccess(result.getData());
                    } else {
                        dataCallback.onError(result != null ? result.getMsg() : "未知错误");
                    }
                }

                @Override
                public void onFailure(String error) {
                    dataCallback.onError(error);
                }
            });
        });
    }
}
