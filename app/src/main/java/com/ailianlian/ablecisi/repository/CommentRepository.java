package com.ailianlian.ablecisi.repository;

import android.content.Context;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.StatusCodeConstant;
import com.ailianlian.ablecisi.pojo.dto.CreateCommentDTO;
import com.ailianlian.ablecisi.pojo.entity.CommentLikeReq;
import com.ailianlian.ablecisi.pojo.vo.CommentVO;
import com.ailianlian.ablecisi.pojo.vo.RootTreeVO;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.result.Result;
import com.ailianlian.ablecisi.utils.HttpClient;
import com.ailianlian.ablecisi.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.HttpUrl;

/**
 * ailianlian
 * com.ailianlian.ablecisi.repository
 * CommentRepository <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/1
 * 星期一
 * 08:56
 */
public class CommentRepository extends BaseRepository {

    public CommentRepository(Context context) {
        super(context);
    }

    /**
     * 创建评论
     *
     * @param targetType 评论目标类型
     * @param targetId   评论目标ID
     * @param sort       排序方式
     * @param page       页码
     * @param size       页大小
     * @param maxDepth   最大深度
     * @param cb         回调
     */
    // 方案A：bundle（顶层+这一页所有子孙）
    public void getBundle(String targetType, long targetId, String sort, int page, int size, Integer maxDepth,
                          DataCallback<PageResult<RootTreeVO>> cb) {
        StringBuilder endpoint = new StringBuilder("/comment/bundle")
                .append("?targetType=").append(targetType)
                .append("&targetId=").append(targetId)
                .append("&sort=").append(sort == null ? "time" : sort)
                .append("&page=").append(page)
                .append("&size=").append(size);
        if (maxDepth != null) endpoint.append("&maxDepth=").append(maxDepth);

        getExecutorService().execute(() -> {
            HttpClient.doGet(getContext(), endpoint.toString(), new HttpClient.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        Type type = TypeToken.getParameterized(Result.class, TypeToken.getParameterized(PageResult.class, RootTreeVO.class).getType()).getType();
                        Result<PageResult<RootTreeVO>> r = JsonUtil.fromJson(response, type);
                        if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                            cb.onSuccess(r.data);
                        } else cb.onError(r == null ? "解析失败" : r.msg);
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

    // ========= 方案B：按 root 增量拉更多层（afterPath 可空；size 控制窗口） =========
    public void getTree(long rootId, String afterPath, int size,
                        DataCallback<List<CommentVO>> cb) {
        StringBuilder endpoint = new StringBuilder("/comment/tree")
                .append("?rootId=").append(rootId)
                .append("&size=").append(size);

        // afterPath 需要 URL encode（可能包含 "/"）
        if (afterPath != null && !afterPath.isEmpty()) {
            String encoded = HttpUrl.parse("http://x")
                    .newBuilder() // 随便写个 base url
                    .addQueryParameter("p", afterPath)
                    .build()
                    .queryParameter("p"); // 只取参数部分
            // encoded 现在是 URL encode 过的字符串
            // 如果encoded为空，说明afterPath全是特殊字符
            endpoint.append("&afterPath=").append(encoded);
            //例如： afterPath = "1/3/5" -> encoded = "1%2F3%2F5"
        }

        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), endpoint.toString(), new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = TypeToken.getParameterized(
                                    Result.class,
                                    TypeToken.getParameterized(List.class, CommentVO.class).getType()
                            ).getType();
                            Result<List<CommentVO>> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                                cb.onSuccess(r.data);
                            } else cb.onError(r == null ? "解析失败" : r.msg);
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

    // ========= 创建评论（顶层/回复均可，返回 CommentVO） =========
    public void createComment(String targetType, long targetId, Long parentId, String content,
                              DataCallback<CommentVO> cb) {
        CreateCommentDTO body = new CreateCommentDTO();
        body.targetType = targetType;
        body.targetId = targetId;
        body.parentId = parentId;   // 顶层传 null
        body.content = content;

        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/comment/create", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = TypeToken.getParameterized(Result.class, CommentVO.class).getType();
                            Result<CommentVO> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                                cb.onSuccess(r.data);
                            } else cb.onError(r == null ? "解析失败" : r.msg);
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

    // ========= 点赞 / 取消点赞 =========
    public void like(long commentId, DataCallback<Boolean> cb) {
        CommentLikeReq body = new CommentLikeReq();
        body.commentId = commentId;

        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/comment/like", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = TypeToken.getParameterized(Result.class, Boolean.class).getType();
                            Result<Boolean> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                                cb.onSuccess(r.data);
                            } else cb.onError(r == null ? "解析失败" : r.msg);
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

    public void unlike(long commentId, DataCallback<Boolean> cb) {
        CommentLikeReq body = new CommentLikeReq();
        body.commentId = commentId;

        getExecutorService().execute(() ->
                HttpClient.doPost(getContext(), "/comment/unlike", body, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = TypeToken.getParameterized(Result.class, Boolean.class).getType();
                            Result<Boolean> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                                cb.onSuccess(r.data);
                            } else cb.onError(r == null ? "解析失败" : r.msg);
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

    // ========= 可选：顶层评论分页（article/post 通用） =========
    public void getTop(String targetType, long targetId, String sort, int page, int size,
                       DataCallback<PageResult<CommentVO>> cb) {
        String endpoint = new StringBuilder("/comment/top")
                .append("?targetType=").append(targetType)
                .append("&targetId=").append(targetId)
                .append("&sort=").append(sort == null ? "time" : sort)
                .append("&page=").append(page)
                .append("&size=").append(size)
                .toString();

        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), endpoint, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = TypeToken.getParameterized(
                                    Result.class,
                                    TypeToken.getParameterized(PageResult.class, CommentVO.class).getType()
                            ).getType();
                            Result<PageResult<CommentVO>> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                                cb.onSuccess(r.data);
                            } else cb.onError(r == null ? "解析失败" : r.msg);
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

    // ========= 可选：直接子评论分页（某个 parentId） =========
    public void getChildren(long parentId, int page, int size,
                            DataCallback<PageResult<CommentVO>> cb) {
        String endpoint = new StringBuilder("/comment/children")
                .append("?parentId=").append(parentId)
                .append("&page=").append(page)
                .append("&size=").append(size)
                .toString();

        getExecutorService().execute(() ->
                HttpClient.doGet(getContext(), endpoint, new HttpClient.HttpCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            Type type = TypeToken.getParameterized(
                                    Result.class,
                                    TypeToken.getParameterized(PageResult.class, CommentVO.class).getType()
                            ).getType();
                            Result<PageResult<CommentVO>> r = JsonUtil.fromJson(response, type);
                            if (r != null && r.code == StatusCodeConstant.SUCCESS) {
                                cb.onSuccess(r.data);
                            } else cb.onError(r == null ? "解析失败" : r.msg);
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
