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
        String articleId = article.getValue() != null ? String.valueOf(article.getValue().getId()) : null;
        if (articleId == null) {
            errorMessage.postValue("文章信息不可用，无法更新关注状态");
            isLoading.postValue(false);
            return;
        }
        articleRepository.followAuthor(articleId, "1", isFollow, new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    isFollowing.postValue(isFollow);
                    errorMessage.postValue(isFollow ? "已关注作者" : "已取消关注作者");
                } else {
                    errorMessage.postValue("关注状态更新失败");
                }
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
     * 加载关注状态
     */
    public void loadFollowStatus(String articleId) {
        isLoading.postValue(true);
        if (articleId == null) {
            errorMessage.postValue("文章信息不可用，无法加载关注状态");
            isLoading.postValue(false);
            return;
        }
        articleRepository.loadFollowStatus("1", "2", new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isFollowingStatus) {
                isFollowing.postValue(isFollowingStatus);
                System.out.println("关注状态加载成功: " + isFollowingStatus);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("关注状态加载失败: " + error);
            }

            @Override
            public void onNetworkError() {
                isLoading.postValue(false);
                errorMessage.postValue("网络异常，请检查您的网络连接");
            }
        });

    }

    public void likeArticle() {
    }

    public void bookmarkArticle() {
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
    }
}