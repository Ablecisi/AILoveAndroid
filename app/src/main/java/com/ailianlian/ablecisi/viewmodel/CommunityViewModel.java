package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.dto.PostLikeResultDTO;
import com.ailianlian.ablecisi.repository.ArticleRepository;
import com.ailianlian.ablecisi.repository.CommunityRepository;
import com.ailianlian.ablecisi.utils.LoginInfoUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("全部");
    private final MutableLiveData<String> userMessage = new MutableLiveData<>();

    private final CommunityRepository repository;
    private final ArticleRepository articleRepository;
    private List<Post> lastLoaded = new ArrayList<>();

    public CommunityViewModel(@NonNull Application application) {
        super(application);
        repository = new CommunityRepository(application);
        articleRepository = new ArticleRepository(application);
        refreshPosts();
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getCurrentCategory() {
        return currentCategory;
    }

    public LiveData<String> getUserMessage() {
        return userMessage;
    }

    public void setCategory(String category) {
        if (category != null && !category.equals(currentCategory.getValue())) {
            currentCategory.setValue(category);
            applyCategoryFilter();
        }
    }

    public void refreshPosts() {
        isLoading.setValue(true);
        repository.loadFeed(1, 30, new BaseRepository.DataCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> data) {
                lastLoaded = data != null ? new ArrayList<>(data) : new ArrayList<>();
                applyCategoryFilter();
                isLoading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                lastLoaded = new ArrayList<>();
                posts.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }

            @Override
            public void onNetworkError() {
                lastLoaded = new ArrayList<>();
                posts.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 列表内点赞：请求接口后按服务端结果更新缓存与当前列表。
     */
    public void requestToggleLike(Post post, boolean wantLike) {
        if (post == null || post.getId() == null) {
            return;
        }
        if (!LoginInfoUtil.isLoggedIn(getApplication())) {
            userMessage.postValue("请先登录后再点赞");
            return;
        }
        long id;
        try {
            id = Long.parseLong(post.getId());
        } catch (NumberFormatException e) {
            userMessage.postValue("帖子数据异常");
            return;
        }
        repository.likePost(id, wantLike, new BaseRepository.DataCallback<PostLikeResultDTO>() {
            @Override
            public void onSuccess(PostLikeResultDTO data) {
                if (data == null) {
                    return;
                }
                patchPostLike(post.getId(), Boolean.TRUE.equals(data.liked), data.likeCount);
            }

            @Override
            public void onError(String msg) {
                userMessage.postValue(msg != null ? msg : "点赞失败");
            }

            @Override
            public void onNetworkError() {
                userMessage.postValue("网络异常");
            }
        });
    }

    private void patchPostLike(String postId, boolean liked, Integer likeCount) {
        for (Post p : lastLoaded) {
            if (postId.equals(p.getId())) {
                p.setLiked(liked);
                if (likeCount != null) {
                    p.setLikeCount(likeCount);
                }
                break;
            }
        }
        applyCategoryFilter();
    }

    public void recordShare(Post post) {
        if (post == null || post.getId() == null) {
            return;
        }
        if (!LoginInfoUtil.isLoggedIn(getApplication())) {
            return;
        }
        long id;
        try {
            id = Long.parseLong(post.getId());
        } catch (NumberFormatException e) {
            return;
        }
        repository.recordShare(id, new BaseRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                for (Post p : lastLoaded) {
                    if (post.getId().equals(p.getId())) {
                        int s = p.getShareCount() != null ? p.getShareCount() : 0;
                        p.setShareCount(s + 1);
                        break;
                    }
                }
                applyCategoryFilter();
            }

            @Override
            public void onError(String msg) {
                /* 分享计数失败不影响系统分享 */
            }

            @Override
            public void onNetworkError() {
            }
        });
    }

    public void followPostAuthor(Post post, boolean follow) {
        if (post == null || post.getUser() == null || post.getUser().getId() == null
                || post.getUser().getId().isEmpty()) {
            userMessage.postValue("无法关注该作者");
            return;
        }
        if (!LoginInfoUtil.isLoggedIn(getApplication())) {
            userMessage.postValue("请先登录");
            return;
        }
        articleRepository.followAuthor(post.getUser().getId(), follow, new BaseRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                String authorId = post.getUser().getId();
                for (Post p : lastLoaded) {
                    if (p.getUser() != null && authorId.equals(p.getUser().getId())) {
                        p.getUser().setFollowed(follow);
                    }
                }
                applyCategoryFilter();
                userMessage.postValue(follow ? "已关注" : "已取消关注");
            }

            @Override
            public void onError(String msg) {
                userMessage.postValue(msg != null ? msg : "操作失败");
            }

            @Override
            public void onNetworkError() {
                userMessage.postValue("网络异常");
            }
        });
    }

    private void applyCategoryFilter() {
        String category = currentCategory.getValue();
        if (category == null || "全部".equals(category)) {
            posts.postValue(new ArrayList<>(lastLoaded));
            return;
        }
        List<Post> filtered = lastLoaded.stream().filter(p -> {
            if ("推荐".equals(category)) {
                return true;
            }
            if ("关注".equals(category)) {
                return p.getUser() != null && Boolean.TRUE.equals(p.getUser().getFollowed());
            }
            if ("热门".equals(category)) {
                return p.getLikeCount() != null && p.getLikeCount() > 50;
            }
            if ("最新".equals(category)) {
                return p.getCreatedAt() != null && p.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7));
            }
            return true;
        }).collect(Collectors.toList());
        posts.postValue(filtered);
    }
}
