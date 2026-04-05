package com.ailianlian.ablecisi.pojo.dto;

import java.util.List;


public class PostFeedItemDTO {
    public String id;
    public String authorId;
    public String authorName;
    public String authorAvatarUrl;
    public String content;
    public List<String> imageUrls;
    public List<String> tags;
    public Integer likeCount;
    public Integer commentCount;
    public Integer shareCount;
    public String createdAt;
    public Boolean liked;
    public Boolean authorFollowed;
}
