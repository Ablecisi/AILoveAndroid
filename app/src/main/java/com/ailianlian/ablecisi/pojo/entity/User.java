package com.ailianlian.ablecisi.pojo.entity;

import java.util.List;

/**
 * 用户数据模型
 */
public class User {
    private String id; // 用户ID
    private String username; // 用户名
    private String name; // 昵称
    private String description; // 用户简介
    private String avatarUrl; // 头像URL
    private Integer followingCount; // 关注人数
    private Integer followersCount; // 粉丝人数
    private List<Long> postIds; // 用户帖子ID列表
    private List<Long> characterIds; // 我的角色ID列表
    private Boolean isFollowed; // 是否已关注
    private String token = "";  // 用户令牌

    public User() {
    }

    public User(String id, String username, String name, String description, String avatarUrl, Integer followingCount, Integer followersCount, List<Long> postIds, List<Long> characterIds, Boolean isFollowed) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.followingCount = followingCount;
        this.followersCount = followersCount;
        this.postIds = postIds;
        this.characterIds = characterIds;
        this.isFollowed = isFollowed;
    }

    public User(String number, String username, String avatarUrl, Boolean isFollowed) {
        this.id = number;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.isFollowed = isFollowed;
        this.name = "用户" + number; // 默认昵称
        this.description = "这是用户" + number + "的简介"; // 默认简介
        this.followingCount = 0; // 默认关注人数
        this.followersCount = 0; // 默认粉丝人数
        this.postIds = List.of(); // 默认帖子ID列表为空
        this.characterIds = List.of(); // 默认角色ID列表为空
    }

    public User(String id, String username, String description, String avatarUrl, int followingCount, int followersCount, int postCount) {
        this.id = id;
        this.username = username;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.followingCount = followingCount;
        this.followersCount = followersCount;
        // 设置帖子数量为postCount
        this.postIds = List.of(); // 默认帖子ID列表为空
        this.characterIds = List.of(); // 默认角色ID列表为空
        this.isFollowed = false; // 默认未关注
        this.name = "用户" + id; // 默认昵称
    }

    public User(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public List<Long> getPostIds() {
        return postIds;
    }

    public void setPostIds(List<Long> postIds) {
        this.postIds = postIds;
    }

    public List<Long> getCharacterIds() {
        return characterIds;
    }

    public void setCharacterIds(List<Long> characterIds) {
        this.characterIds = characterIds;
    }

    public Boolean getFollowed() {
        return isFollowed;
    }

    public void setFollowed(Boolean followed) {
        isFollowed = followed;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}