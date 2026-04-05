package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.PostCreateBodyDTO;
import com.ailianlian.ablecisi.pojo.dto.PostIdsQueryDTO;
import com.ailianlian.ablecisi.pojo.dto.PostInteractionStateDTO;
import com.ailianlian.ablecisi.pojo.dto.PostLikeBodyDTO;
import com.ailianlian.ablecisi.pojo.dto.PostShareBodyDTO;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.entity.User;
import com.ailianlian.ablecisi.pojo.dto.PostFeedItemDTO;
import com.ailianlian.ablecisi.pojo.dto.PostLikeResultDTO;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityRepository extends BaseRepository {

    public CommunityRepository(Context context) {
        super(context);
    }

    public void loadMyPosts(int page, int size, DataCallback<List<Post>> callback) {
        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), "/post/mine?page=" + page + "&size=" + size, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Type type = new TypeToken<Result<List<PostFeedItemDTO>>>() {
                        }.getType();
                        Result<List<PostFeedItemDTO>> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS && r.getData() != null) {
                            callback.onSuccess(mapFeedList(r.getData()));
                        } else {
                            callback.onError(r != null ? r.getMsg() : "加载失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }));
    }

    public void loadFeed(int page, int size, DataCallback<List<Post>> callback) {
        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), "/post/feed?page=" + page + "&size=" + size, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Type type = new TypeToken<Result<List<PostFeedItemDTO>>>() {
                        }.getType();
                        Result<List<PostFeedItemDTO>> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS && r.getData() != null) {
                            List<Post> posts = mapFeedList(r.getData());
                            maybeMergeInteractionState(posts, callback);
                        } else {
                            callback.onError(r != null ? r.getMsg() : "加载失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }));
    }

    private void maybeMergeInteractionState(List<Post> posts, DataCallback<List<Post>> callback) {
        if (!LoginInfoUtil.isLoggedIn(getContext()) || posts.isEmpty()) {
            callback.onSuccess(posts);
            return;
        }
        List<Long> ids = new ArrayList<>();
        for (Post p : posts) {
            try {
                ids.add(Long.parseLong(p.getId()));
            } catch (Exception ignored) {
            }
        }
        if (ids.isEmpty()) {
            callback.onSuccess(posts);
            return;
        }
        PostIdsQueryDTO body = new PostIdsQueryDTO();
        body.postIds = ids;
        HttpClient.doPost(getContext(), "/post/interaction-state", body, new HttpClient.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                Type type = new TypeToken<Result<List<PostInteractionStateDTO>>>() {
                }.getType();
                Result<List<PostInteractionStateDTO>> r = JsonUtil.fromJson(response, type);
                if (r != null && r.getCode() == StatusCodeConstant.SUCCESS && r.getData() != null) {
                    Map<String, PostInteractionStateDTO> m = new HashMap<>();
                    for (PostInteractionStateDTO s : r.getData()) {
                        if (s != null && s.postId != null) {
                            m.put(s.postId, s);
                        }
                    }
                    for (Post p : posts) {
                        PostInteractionStateDTO s = m.get(p.getId());
                        if (s != null) {
                            p.setLiked(Boolean.TRUE.equals(s.liked));
                            if (p.getUser() != null) {
                                p.getUser().setFollowed(Boolean.TRUE.equals(s.followingAuthor));
                            }
                        }
                    }
                }
                callback.onSuccess(posts);
            }

            @Override
            public void onFailure(String error) {
                callback.onSuccess(posts);
            }
        });
    }

    public void loadPostDetail(long postId, DataCallback<Post> callback) {
        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), "/post/detail?id=" + postId, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Type type = new TypeToken<Result<PostFeedItemDTO>>() {
                        }.getType();
                        Result<PostFeedItemDTO> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS && r.getData() != null) {
                            Post p = mapSingle(r.getData());
                            List<Post> one = new ArrayList<>();
                            one.add(p);
                            maybeMergeInteractionState(one, new DataCallback<List<Post>>() {
                                @Override
                                public void onSuccess(List<Post> data) {
                                    callback.onSuccess(data.isEmpty() ? p : data.get(0));
                                }

                                @Override
                                public void onError(String msg) {
                                    callback.onSuccess(p);
                                }

                                @Override
                                public void onNetworkError() {
                                    callback.onSuccess(p);
                                }
                            });
                        } else {
                            callback.onError(r != null ? r.getMsg() : "加载失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }));
    }

    public void likePost(long postId, boolean like, DataCallback<PostLikeResultDTO> callback) {
        PostLikeBodyDTO body = new PostLikeBodyDTO();
        body.postId = postId;
        body.like = like;
        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/post/like", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Type type = new TypeToken<Result<PostLikeResultDTO>>() {
                        }.getType();
                        Result<PostLikeResultDTO> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS && r.getData() != null) {
                            callback.onSuccess(r.getData());
                        } else {
                            callback.onError(r != null ? r.getMsg() : "操作失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }));
    }

    public void recordShare(long postId, DataCallback<Void> callback) {
        PostShareBodyDTO body = new PostShareBodyDTO();
        body.postId = postId;
        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/post/share", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Type type = new TypeToken<Result<Void>>() {
                        }.getType();
                        Result<Void> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.getCode() == StatusCodeConstant.SUCCESS) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(r != null ? r.getMsg() : "分享记录失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }));
    }

    public void createPost(PostCreateBodyDTO body, DataCallback<Long> callback) {
        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/post/create", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Result<Map<String, Object>> raw = JsonUtil.fromJson(response,
                                new TypeToken<Result<Map<String, Object>>>() {
                                }.getType());
                        if (raw != null && raw.getCode() == StatusCodeConstant.SUCCESS && raw.getData() != null) {
                            Object idObj = raw.getData().get("id");
                            long id = -1;
                            if (idObj instanceof Number) {
                                id = ((Number) idObj).longValue();
                            }
                            if (id > 0) {
                                callback.onSuccess(id);
                            } else {
                                callback.onError("发布失败");
                            }
                        } else {
                            callback.onError(raw != null ? raw.getMsg() : "发布失败");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }));
    }

    private static List<Post> mapFeedList(List<PostFeedItemDTO> data) {
        List<Post> posts = new ArrayList<>();
        for (PostFeedItemDTO d : data) {
            if (d == null || d.id == null) {
                continue;
            }
            posts.add(mapSingle(d));
        }
        return posts;
    }

    private static Post mapSingle(PostFeedItemDTO d) {
        User u = new User();
        u.setId(d.authorId != null ? d.authorId : "");
        String nick = d.authorName != null && !d.authorName.isBlank() ? d.authorName.trim() : "用户";
        u.setName(nick);
        u.setUsername(d.authorName != null ? d.authorName : "");
        u.setAvatarUrl(d.authorAvatarUrl != null ? d.authorAvatarUrl : "");
        u.setFollowed(Boolean.TRUE.equals(d.authorFollowed));
        LocalDateTime at = parseTime(d.createdAt);
        return new Post(
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
        );
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
