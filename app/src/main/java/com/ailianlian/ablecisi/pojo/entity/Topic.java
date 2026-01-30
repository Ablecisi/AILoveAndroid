package com.ailianlian.ablecisi.pojo.entity;

/**
 * 热门话题数据模型
 */
public class Topic {
    private String id;
    private String title; // 话题标题
    private Integer rank; // 排名，1表示第一名，2表示第二名，以此类推
    private String views; // 浏览量
    private String comments; // 评论数
    private String likes; // 点赞数

    public Topic(String id, String title, Integer rank, String views, String comments, String likes) {
        this.id = id;
        this.title = title;
        this.rank = rank;
        this.views = views;
        this.comments = comments;
        this.likes = likes;
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

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }
}