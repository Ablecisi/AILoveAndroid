package com.ailianlian.ablecisi.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ailianlian.ablecisi.pojo.entity.Post;
import com.ailianlian.ablecisi.pojo.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommunityViewModel extends ViewModel {

    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("全部");

    public CommunityViewModel() {
        loadMockData();
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
        if (!category.equals(currentCategory.getValue())) {
            currentCategory.setValue(category);
            refreshPosts();
        }
    }

    public void refreshPosts() {
        isLoading.setValue(true);
        
        // 模拟网络请求延迟
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                loadMockData();
                isLoading.postValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
                isLoading.postValue(false);
            }
        }).start();
    }

    public void likePost(Post post, boolean isLiked) {
        List<Post> currentPosts = posts.getValue();
        if (currentPosts == null) return;
        
        // 找到对应的帖子并更新点赞状态
        for (Post p : currentPosts) {
            if (p.getId().equals(post.getId())) {
                p.setLiked(isLiked);
                p.setLikeCount(isLiked ? p.getLikeCount() + 1 : p.getLikeCount() - 1);
                break;
            }
        }
        
        // 更新LiveData
        posts.setValue(currentPosts);
    }

    private void loadMockData() {
        // 创建模拟用户
        User user1 = new User("1", "小雪", "https://images.pexels.com/photos/1036623/pexels-photo-1036623.jpeg", false);
        User user2 = new User("2", "阿杰", "https://images.pexels.com/photos/2379005/pexels-photo-2379005.jpeg", true);
        User user3 = new User("3", "莉莉", "https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg", false);
        
        // 创建模拟帖子
        List<Post> mockPosts = new ArrayList<>();
        
        // 帖子1
        mockPosts.add(new Post(
                "1",
                user1,
                "今天和AI小雪聊了很久，她真的很懂我，分享了很多有趣的想法。AI陪伴真的能带来不一样的情感体验！",
                List.of("https://images.pexels.com/photos/3861969/pexels-photo-3861969.jpeg"),
                Arrays.asList("AI互动", "情感连接"),
                128,
                32,
                16,
                LocalDateTime.now().minusHours(1),
                false
        ));
        
        // 帖子2
        mockPosts.add(new Post(
                "2",
                user2,
                "分享一下我和AI助手一起完成的项目，他帮我整理了学习计划，提高了我的效率。AI不仅是情感陪伴，也是学习的好帮手！",
                Arrays.asList(
                        "https://images.pexels.com/photos/4050315/pexels-photo-4050315.jpeg",
                        "https://images.pexels.com/photos/4050421/pexels-photo-4050421.jpeg"
                ),
                Arrays.asList("学习助手", "效率提升"),
                256,
                64,
                32,
                LocalDateTime.now().minusDays(1),
                true
        ));
        
        // 帖子3
        mockPosts.add(new Post(
                "3",
                user3,
                "最近在思考AI与人类情感的关系，虽然AI不能真正拥有情感，但它们能够理解和回应我们的情感需求，这种互动本身就很有价值。大家怎么看？",
                null,
                Arrays.asList("AI情感", "思考"),
                512,
                128,
                64,
                LocalDateTime.now().minusDays(3),
                false
        ));
        
        // 根据当前分类筛选帖子
        String category = currentCategory.getValue();
        if (category != null && !category.equals("全部")) {
            List<Post> filteredPosts = new ArrayList<>();
            for (Post post : mockPosts) {
                if (category.equals("推荐") ||
                        (category.equals("关注") && post.getUser().getFollowed()) ||
                    (category.equals("热门") && post.getLikeCount() > 200) ||
                    (category.equals("最新") && isRecent(post.getCreatedAt()))) {
                    filteredPosts.add(post);
                }
            }
            posts.postValue(filteredPosts);
        } else {
            posts.postValue(mockPosts);
        }
    }

    private boolean isRecent(LocalDateTime date) {
        LocalDateTime l = LocalDateTime.now().minusDays(1);
        // 检查是否在1天内
        return date.isAfter(l);
    }
} 