package com.ailianlian.ablecisi.pojo.dto;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.dto
 * UserFollowDTO <br>
 * 用户关注数据传输对象
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 11:09
 */
public class UserFollowDTO {
    private String userId; // 用户ID
    private String authorId; // 作者ID
    private Boolean isFollowing; // 是否关注

    public UserFollowDTO() {
    }

    public UserFollowDTO(String userId, String authorId, Boolean isFollowing) {
        this.userId = userId;
        this.authorId = authorId;
        this.isFollowing = isFollowing;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(Boolean following) {
        isFollowing = following;
    }
}
