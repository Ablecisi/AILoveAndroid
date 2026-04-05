package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.pojo.vo.PostFeedItemDTO;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommunityRepository extends BaseRepository {

    public CommunityRepository(Context context) {
        super(context);
    }

    public void loadFeed(int page, int size, DataCallback<List<Post>> callback) {
        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), "/post/feed?page=" + page + "&size=" + size, new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    Type type = new TypeToken<Result<List<PostFeedItemDTO>>>() {
                    }.getType();
                    Result<List<PostFeedItemDTO>> r = JsonUtil.fromJson(response, type);
                    if (r != null && r.getCode() == StatusCodeConstant.SUCCESS && r.getData() != null) {
                        List<Post> posts = new ArrayList<>();
                        for (PostFeedItemDTO d : r.getData()) {
                            if (d == null || d.id == null) {
                                continue;
                            }
                            User u = new User(
                                    d.authorId != null ? d.authorId : "",
                                    d.authorName != null ? d.authorName : "",
                                    d.authorAvatarUrl != null ? d.authorAvatarUrl : "",
                                    Boolean.TRUE.equals(d.liked)
                            );
                            LocalDateTime at = parseTime(d.createdAt);
                            posts.add(new Post(
                                    d.id,
                                    u,
                                    d.content != null ? d.content : "",
                                    d.imageUrls != null ? d.imageUrls : new ArrayList<>(),
                                    d.tags != null ? d.tags : new ArrayList<>(),
                                    d.likeCount != null ? d.likeCount : 0,
                                    d.commentCount != null ? d.commentCount : 0,
                                    d.shareCount != null ? d.shareCount : 0,
                                    at,
                                    Boolean.TRUE.equals(d.liked)
                            ));
                        }
                        callback.onSuccess(posts);
                    } else {
                        callback.onError(r != null ? r.getMsg() : "加载失败");
                    }
                }

                @Override
                public void onFailure(String error) {
                    callback.onError(error);
                }
            });
        });
    }

    private static LocalDateTime parseTime(String iso) {
        if (iso == null || iso.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            if (iso.length() > 19) {
                return LocalDateTime.parse(iso.substring(0, 19), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            return LocalDateTime.parse(iso.replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
