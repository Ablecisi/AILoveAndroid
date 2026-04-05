package com.ailianlian.ablecisi.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ailianlian.ablecisi.baseclass.BaseRepository;
import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.repository.CommunityRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("全部");
    private final CommunityRepository repository;
    private List<Post> lastLoaded = new ArrayList<>();

    public CommunityViewModel(@NonNull Application application) {
        super(application);
        repository = new CommunityRepository(application);
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

    public void likePost(Post post, boolean isLiked) {
        List<Post> currentPosts = posts.getValue();
        if (currentPosts == null) {
            return;
        }
        for (Post p : currentPosts) {
            if (p.getId().equals(post.getId())) {
                p.setLiked(isLiked);
                int c = p.getLikeCount() != null ? p.getLikeCount() : 0;
                p.setLikeCount(isLiked ? c + 1 : Math.max(0, c - 1));
                break;
            }
        }
        posts.setValue(currentPosts);
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
