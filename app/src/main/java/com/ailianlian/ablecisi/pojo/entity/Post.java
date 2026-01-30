package com.ailianlian.ablecisi.pojo.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区帖子数据模型
 */
public class Post {
    private String id;
    private User user; // 帖子作者信息
    private String content; // 帖子内容
    private List<String> imageUrls; // 帖子图片URL列表
    private List<String> tags; // 帖子标签列表
    private Integer likeCount; // 点赞数
    private Integer commentCount; // 评论数
    private Integer shareCount; // 分享数
    private LocalDateTime createdAt; // 创建时间
    private Boolean isLiked; // 是否已点赞

    public Post(String id, User user, String content, List<String> imageUrls, List<String> tags, Integer likeCount, Integer commentCount, Integer shareCount, LocalDateTime createdAt, Boolean isLiked) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.imageUrls = imageUrls;
        this.tags = tags;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.createdAt = createdAt;
        this.isLiked = isLiked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getLiked() {
        return isLiked;
    }

    public void setLiked(Boolean liked) {
        isLiked = liked;
    }
}