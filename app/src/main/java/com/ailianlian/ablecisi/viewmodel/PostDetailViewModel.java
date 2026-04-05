package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.constant.ArticleOrPostTypeConstant;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.vo.CommentVO;
import com.ailianlian.ablecisi.pojo.dto.PostLikeResultDTO;
import com.ailianlian.ablecisi.pojo.vo.RootTreeVO;
import com.ailianlian.ablecisi.repository.ArticleRepository;
import com.ailianlian.ablecisi.repository.CommentRepository;
import com.ailianlian.ablecisi.repository.CommunityRepository;
import com.ailianlian.ablecisi.result.PageResult;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

public class PostDetailViewModel extends AndroidViewModel {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    private final MutableLiveData<Post> post = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<PageResult<RootTreeVO>> pageResult = new MutableLiveData<>();
    private final MutableLiveData<java.util.List<CommentVO>> comments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>(false);

    private String postIdStr;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        communityRepository = new CommunityRepository(application);
        commentRepository = new CommentRepository(application);
        articleRepository = new ArticleRepository(application);
    }

    public void initPostId(String id) {
        this.postIdStr = id;
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<PageResult<RootTreeVO>> getPageResult() {
        return pageResult;
    }

    public LiveData<java.util.List<CommentVO>> getComments() {
        return comments;
    }

    public LiveData<Boolean> getIsFollowing() {
        return isFollowing;
    }

    public void loadPost() {
        if (postIdStr == null) {
            return;
        }
        isLoading.postValue(true);
        long id = Long.parseLong(postIdStr);
        communityRepository.loadPostDetail(id, new BaseRepository.DataCallback<Post>() {
            @Override
            public void onSuccess(Post data) {
                post.postValue(data);
                if (data.getUser() != null) {
                    isFollowing.postValue(Boolean.TRUE.equals(data.getUser().getFollowed()));
                }
                isLoading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                errorMessage.postValue(msg);
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                errorMessage.postValue("网络异常");
                isLoading.postValue(false);
            }
        });
    }

    public void loadComments() {
        if (postIdStr == null) {
            return;
        }
        long id = Long.parseLong(postIdStr);
        commentRepository.getBundle(
                ArticleOrPostTypeConstant.TYPE_POST,
                id,
                "time",
                1,
                20,
                3,
                new BaseRepository.DataCallback<PageResult<RootTreeVO>>() {
                    @Override
                    public void onSuccess(PageResult<RootTreeVO> data) {
                        pageResult.postValue(data);
                    }

                    @Override
                    public void onError(String msg) {
                        errorMessage.postValue("评论加载失败: " + msg);
                    }

                    @Override
                    public void onNetworkError() {
                        errorMessage.postValue("网络异常");
                    }
                });
    }

    public void refreshAll() {
        loadPost();
        loadComments();
    }

    public void followAuthor(boolean follow) {
        Post p = post.getValue();
        if (p == null || p.getUser() == null || p.getUser().getId() == null) {
            return;
        }
        if (!LoginInfoUtil.isLoggedIn(getApplication())) {
            errorMessage.postValue("请先登录");
            return;
        }
        articleRepository.followAuthor(p.getUser().getId(), follow, new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                isFollowing.postValue(follow);
                if (p.getUser() != null) {
                    p.getUser().setFollowed(follow);
                }
                post.postValue(p);
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

    public void togglePostLike() {
        Post p = post.getValue();
        if (p == null) {
            return;
        }
        if (!LoginInfoUtil.isLoggedIn(getApplication())) {
            errorMessage.postValue("请先登录后再点赞");
            return;
        }
        boolean want = !Boolean.TRUE.equals(p.getLiked());
        long id = Long.parseLong(postIdStr);
        communityRepository.likePost(id, want, new BaseRepository.DataCallback<PostLikeResultDTO>() {
            @Override
            public void onSuccess(PostLikeResultDTO data) {
                if (data != null) {
                    p.setLiked(Boolean.TRUE.equals(data.liked));
                    if (data.likeCount != null) {
                        p.setLikeCount(data.likeCount);
                    }
                    post.postValue(p);
                }
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

    public void createComment(String text, String parentCommentId) {
        if (text == null || text.trim().isEmpty()) {
            errorMessage.postValue("评论内容不能为空");
            return;
        }
        if (!LoginInfoUtil.isLoggedIn(getApplication())) {
            errorMessage.postValue("请先登录");
            return;
        }
        if (postIdStr == null) {
            return;
        }
        long targetId = Long.parseLong(postIdStr);
        Long parentId = parentCommentId == null || parentCommentId.isEmpty()
                ? null : Long.parseLong(parentCommentId);
        isLoading.postValue(true);
        commentRepository.createComment(
                ArticleOrPostTypeConstant.TYPE_POST,
                targetId,
                parentId,
                text.trim(),
                new BaseRepository.DataCallback<CommentVO>() {
                    @Override
                    public void onSuccess(CommentVO data) {
                        isLoading.postValue(false);
                        errorMessage.postValue("评论成功");
                        refreshAll();
                    }

                    @Override
                    public void onError(String msg) {
                        isLoading.postValue(false);
                        errorMessage.postValue(msg);
                    }

                    @Override
                    public void onNetworkError() {
                        isLoading.postValue(false);
                        errorMessage.postValue("网络异常");
                    }
                });
    }

    public void getCommentsTree(long rootId, String afterPath, int size) {
        commentRepository.getTree(rootId, afterPath, size, new BaseRepository.DataCallback<java.util.List<CommentVO>>() {
            @Override
            public void onSuccess(java.util.List<CommentVO> data) {
                comments.postValue(data);
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

    public void recordShare() {
        if (postIdStr == null || !LoginInfoUtil.isLoggedIn(getApplication())) {
            return;
        }
        communityRepository.recordShare(Long.parseLong(postIdStr), new BaseRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Post p = post.getValue();
                if (p != null) {
                    int s = p.getShareCount() != null ? p.getShareCount() : 0;
                    p.setShareCount(s + 1);
                    post.postValue(p);
                }
            }

            @Override
            public void onError(String msg) {
                /* 忽略分享计数失败 */
            }

            @Override
            public void onNetworkError() {
            }
        });
    }
}
