package com.ailianlian.ablecisi.pojo.vo;

import java.util.List;

/** 与后端 {@link com.ablecisi.ailovebacked.pojo.vo.PostFeedVO} JSON 对齐 */
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
}
