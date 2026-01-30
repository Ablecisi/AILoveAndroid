package com.ailianlian.ablecisi.pojo.entity;

import com.ailianlian.ablecisi.pojo.vo.CommentVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章数据模型
 */
public class Article {
    private String id;
    private String title; // 文章标题
    private String description; // 文章描述
    private String content; // 文章内容，可能是HTML格式
    private String coverImageUrl; // 封面图片URL
    private String authorId; // 作者ID
    private String authorName; // 作者名称
    private String authorAvatarUrl; // 作者头像URL
    private LocalDateTime publishTime; // 发布时间，使用时间戳表示
    private Integer viewCount; // 浏览量 int--> 最大值为2,147,483,647
    private Integer likeCount; // 点赞数
    private Integer commentCount; // 评论数
    private Boolean isLiked; // 是否已点赞
    private Boolean isBookmarked; // 是否已收藏
    private List<String> tags; // 文章标签列表
    private List<CommentVO> comments; // 文章评论列表

    public Article() {
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public Article(String id, String title, String description, String content, String coverImageUrl, String authorId, String authorName, String authorAvatarUrl, LocalDateTime publishTime, Integer viewCount, Integer likeCount, Integer commentCount, Boolean isLiked, Boolean isBookmarked, List<String> tags, List<CommentVO> comments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.coverImageUrl = coverImageUrl;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.publishTime = publishTime;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLiked = isLiked;
        this.isBookmarked = isBookmarked;
        this.tags = tags;
        this.comments = comments;
    }

    public <T> Article(String id, String title, String description, String coverImageUrl, List<T> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = "";
        this.coverImageUrl = coverImageUrl;
        this.tags = new ArrayList<>();
        for (T tag : tags) {
            this.tags.add(tag.toString());
        }
        this.authorId = "";
        this.authorName = "";
        this.authorAvatarUrl = "";
        this.publishTime = LocalDateTime.now();
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isLiked = false;
        this.isBookmarked = false;
        this.comments = new ArrayList<>();
    }

    public Article(String title, String description, String coverImageUrl, String url, String s1, String s2, String url1, long l, int i, int i1, int i2) {
        this.title = title;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.content = url; // 假设“url”是本例中的内容
        this.authorId = s1;
        this.authorName = s2;
        this.authorAvatarUrl = url1;
        this.publishTime = LocalDateTime.ofEpochSecond(l, 0, java.time.ZoneOffset.UTC);
        this.viewCount = i;
        this.likeCount = i1;
        this.commentCount = i2;
        this.isLiked = false; // 默认为false
        this.isBookmarked = false; // 默认为false
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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

    public Boolean getLiked() {
        return isLiked;
    }

    public void setLiked(Boolean liked) {
        isLiked = liked;
    }

    public Boolean getBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(Boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<CommentVO> getComments() {
        return comments;
    }

    public void setComments(List<CommentVO> comments) {
        this.comments = comments;
    }
}