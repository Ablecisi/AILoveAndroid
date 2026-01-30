package com.ailianlian.ablecisi.pojo.vo;

import java.time.LocalDateTime;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.vo
 * CommentVO <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/1
 * 星期一
 * 08:34
 */
public class CommentVO {
    public Long id;
    public String content;
    public Long userId;
    public String userName;    // 冗余: 前端直接显示昵称
    public String avatarUrl;   // 冗余: 前端直接显示头像

    public Long parentId;
    public Long rootId;
    public Integer depth;
    public Integer likeCount;
    public Integer replyCount;

    public Boolean deleted;    // 用 Boolean 替代 is_deleted，语义更清晰
    public LocalDateTime createTime;
    public String pathCursor; // ★ 用于 /tree 的 afterPath
}
