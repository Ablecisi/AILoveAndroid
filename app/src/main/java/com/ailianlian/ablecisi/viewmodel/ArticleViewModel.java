package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.baseclass.BaseViewModel;
import com.ailianlian.ablecisi.constant.ArticleOrPostTypeConstant;
import com.ailianlian.ablecisi.pojo.entity.Article;
import com.ailianlian.ablecisi.pojo.vo.CommentVO;
import com.ailianlian.ablecisi.pojo.vo.RootTreeVO;
import com.ailianlian.ablecisi.repository.ArticleRepository;
import com.ailianlian.ablecisi.repository.CommentRepository;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章详情的ViewModel
 */
public class ArticleViewModel extends BaseViewModel {

    private final MutableLiveData<Article> article; // 文章详情
    private final MutableLiveData<List<Article>> relatedArticles; // 相关文章列表
    private final MutableLiveData<PageResult<RootTreeVO>> pageResultMutableLiveData; // 评论列表
    private final MutableLiveData<List<CommentVO>> comments; // 评论列表
    private final MutableLiveData<CommentVO> newComment; // 新增的评论
    private final MutableLiveData<Boolean> isLoading; // 是否正在加载文章详情
    private final MutableLiveData<Boolean> isCommentLoading; // 是否正在加载评论
    private final MutableLiveData<String> errorMessage; // 错误信息
    private final MutableLiveData<Boolean> commentSuccess; // 评论是否成功
    private final MutableLiveData<Boolean> isFollowing; // 是否关注作者
    private final ArticleRepository articleRepository; // 文章仓库
    private final CommentRepository commentRepository; // 评论仓库

    public LiveData<Article> getArticle() {
        return article;
    }
    public LiveData<List<Article>> getRelatedArticles() {
        return relatedArticles;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<Boolean> getIsCommentLoading() {
        return isCommentLoading;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<Boolean> getCommentSuccess() {
        return commentSuccess;
    }
    public LiveData<Boolean> getIsFollowing() {
        return isFollowing;
    }

    public LiveData<PageResult<RootTreeVO>> getPageResultMutableLiveData() {
        return pageResultMutableLiveData;
    }

    public LiveData<List<CommentVO>> getComments() {
        return comments;
    }

    public LiveData<CommentVO> getNewComment() {
        return newComment;
    }

    public ArticleViewModel(@NonNull Application application) {
        super(application, new MutableLiveData<>());
        article = new MutableLiveData<>();
        relatedArticles = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        isCommentLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        commentSuccess = new MutableLiveData<>(false);
        isFollowing = new MutableLiveData<>(false);
        pageResultMutableLiveData = new MutableLiveData<>();
        comments = new MutableLiveData<>(new ArrayList<>());
        newComment = new MutableLiveData<>();
        articleRepository = new ArticleRepository(application);
        commentRepository = new CommentRepository(application);
    }


    /**
     * 加载文章详情
     *
     * @param articleId 文章ID
     */
    public void loadArticle(String articleId) {
        isLoading.postValue(true);
        // 通过repository加载文章数据
        articleRepository.loadArticle(articleId, new BaseRepository.DataCallback<Article>() {
            @Override
            public void onSuccess(Article loadedArticle) {
                article.postValue(loadedArticle);
                isLoading.postValue(false);
                loadFollowStatusForAuthor(loadedArticle != null ? loadedArticle.getAuthorId() : null);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("文章加载失败: " + error);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });
    }

    /**
     * 加载相关文章
     *
     * @param articleId 文章ID
     */
    public void loadRelatedArticles(String articleId) {
        isLoading.postValue(true);
        articleRepository.loadRelatedArticles(articleId, new BaseRepository.DataCallback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> articles) {
                if (articles != null && !articles.isEmpty()) {
                    relatedArticles.postValue(articles);
                }
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("相关文章加载失败: " + error);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });

    }


    /**
     * 关注或取消关注作者
     *
     * @param isFollow 是否关注
     */
    public void followAuthor(boolean isFollow) {
        isLoading.postValue(true);
        Article a = article.getValue();
        String authorId = a != null ? a.getAuthorId() : null;
        if (authorId == null || authorId.isEmpty()) {
            errorMessage.postValue("文章信息不可用，无法更新关注状态");
            isLoading.postValue(false);
            return;
        }
        articleRepository.followAuthor(authorId, isFollow, new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean ignored) {
                // 后端关注成功时 data 为 true，取关成功时 data 为 false，均以 HTTP 成功为准
                isFollowing.postValue(isFollow);
                errorMessage.postValue(isFollow ? "已关注作者" : "已取消关注作者");
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("关注状态更新失败: " + error);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });

    }

    /**
     * 根据作者 ID 加载当前登录用户是否已关注（需后端校验 userId 与 token 一致）。
     */
    public void loadFollowStatusForAuthor(String authorId) {
        if (authorId == null || authorId.isEmpty()) {
            isFollowing.postValue(false);
            return;
        }
        String uid = LoginInfoUtil.getUserId(getApplication());
        if (uid == null || "-1".equals(uid) || uid.isEmpty()) {
            isFollowing.postValue(false);
            return;
        }
        articleRepository.loadFollowStatus(uid, authorId, new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isFollowingStatus) {
                isFollowing.postValue(Boolean.TRUE.equals(isFollowingStatus));
            }

            @Override
            public void onError(String error) {
                isFollowing.postValue(false);
            }

            @Override
            public void onNetworkError() {
                isFollowing.postValue(false);
            }
        });
    }

    public void likeArticle() {
        Article a = article.getValue();
        if (a == null || a.getId() == null) {
            errorMessage.postValue("文章未加载完成");
            return;
        }
        boolean next = !Boolean.TRUE.equals(a.getLiked());
        long id = Long.parseLong(a.getId());
        articleRepository.toggleArticleLike(id, next, new BaseRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                a.setLiked(next);
                int c = a.getLikeCount() != null ? a.getLikeCount() : 0;
                a.setLikeCount(next ? c + 1 : Math.max(0, c - 1));
                article.postValue(a);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }

            @Override
            public void onNetworkError() {
                errorMessage.postValue("网络异常");
            }
        });
    }

    public void bookmarkArticle() {
        Article a = article.getValue();
        if (a == null || a.getId() == null) {
            errorMessage.postValue("文章未加载完成");
            return;
        }
        boolean next = !Boolean.TRUE.equals(a.getBookmarked());
        long id = Long.parseLong(a.getId());
        articleRepository.toggleArticleCollect(id, next, new BaseRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                a.setBookmarked(next);
                article.postValue(a);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
            }

            @Override
            public void onNetworkError() {
                errorMessage.postValue("网络异常");
            }
        });
    }

    /**
     * 加载评论
     *
     * @param articleId 文章ID
     */
    public void loadComments(String articleId) {
        isLoading.postValue(true);
        commentRepository.getBundle(
                ArticleOrPostTypeConstant.TYPE_ARTICLE,
                Long.parseLong(articleId),
                null,
                1,
                10,
                3,
                new BaseRepository.DataCallback<PageResult<RootTreeVO>>() {
                    @Override
                    public void onSuccess(PageResult<RootTreeVO> data) {
                        if (data != null) {
                            pageResultMutableLiveData.postValue(data);
                        }
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(String msg) {
                        isLoading.postValue(false);
                        errorMessage.postValue("评论加载失败: " + msg);
                    }

                    @Override
                    public void onNetworkError() {
                        isLoading.postValue(false);
                        errorMessage.postValue("网络异常，请检查您的网络连接");
                    }
                }
        );

    }

    /**
     * 获取评论树
     *
     * @param rootId          根评论ID
     * @param afterPathCursor 分页游标
     * @param size            每页大小
     */
    public void getCommentsTree(long rootId, String afterPathCursor, int size) {
        isLoading.postValue(true);
        commentRepository.getTree(rootId, afterPathCursor, size, new BaseRepository.DataCallback<List<CommentVO>>() {
            @Override
            public void onSuccess(List<CommentVO> data) {
                if (data != null) {
                    comments.postValue(data);
                }
                isLoading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                isLoading.postValue(false);
                errorMessage.postValue("评论加载失败: " + msg);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });
    }

    /**
     * 添加评论
     *
     * @param content         评论内容
     * @param parentCommentId 父评论ID，如果是回复评论的话
     */
    public void createComment(String content, String parentCommentId) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.postValue("评论内容不能为空");
            return;
        }
        isLoading.postValue(true);
        String articleId = article.getValue() != null ? String.valueOf(article.getValue().getId()) : null;
        if (articleId == null) {
            errorMessage.postValue("文章信息不可用，无法添加评论");
            isLoading.postValue(false);
            return;
        }
        long targetId = Long.parseLong(articleId);
        Long parentId = parentCommentId == null || parentCommentId.isEmpty() ? null : Long.parseLong(parentCommentId);
        commentRepository.createComment(
                ArticleOrPostTypeConstant.TYPE_ARTICLE, targetId,
                parentId, content,
                new BaseRepository.DataCallback<CommentVO>() {
                    @Override
                    public void onSuccess(CommentVO data) {
                        commentSuccess.postValue(true);
                        newComment.postValue(data);
                        isLoading.postValue(false);
                        errorMessage.postValue("评论成功");
                    }

                    @Override
                    public void onError(String msg) {
                        isLoading.postValue(false);
                        errorMessage.postValue("评论失败: " + msg);
                    }

                    @Override
                    public void onNetworkError() {
                        isLoading.postValue(false);
                        errorMessage.postValue("网络异常，请检查您的网络连接");
                    }
                }
        );

    }

    public void likeComment(String commentId) {
        if (commentId == null || commentId.isEmpty()) {
            return;
        }
        commentRepository.like(Long.parseLong(commentId), new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
            }

            @Override
            public void onError(String msg) {
                errorMessage.postValue(msg);
            }

            @Override
            public void onNetworkError() {
                errorMessage.postValue("网络异常");
            }
        });
    }
}