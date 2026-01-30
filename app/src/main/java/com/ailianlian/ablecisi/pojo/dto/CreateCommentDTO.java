package com.ailianlian.ablecisi.pojo.dto;

/**
 * AILoveBacked <br>
 * com.ablecisi.ailovebacked.pojo.dto <br>
 *
 * @author Ablecisi
 * @version 0.0.1
 * 2025/8/28
 * 星期四
 * 23:45
 **/
public class CreateCommentDTO {
    public String targetType;  // "article" | "post"
    public Long targetId;      // articleId 或 postId
    public Long parentId;      // 可空：顶层为 null
    public String content;     // 评论内容
}
