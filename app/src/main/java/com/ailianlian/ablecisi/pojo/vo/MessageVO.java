package com.ailianlian.ablecisi.pojo.vo;

import java.time.LocalDateTime;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.vo
 * MessageVO <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/16
 * 星期二
 * 22:30
 */
public class MessageVO {
    public Long id;
    public Integer type;           // 0用户/1AI
    public String content;
    public String emotion;         // ★
    public Double confidence;      // ★
    public Long conversationId;
    public Short isRead;
    public LocalDateTime createTime;
}
