package com.ailianlian.ablecisi.pojo.vo;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo.vo
 * ChatReplyVO <br>
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/9/16
 * 星期二
 * 22:12
 */
public class ChatReplyVO {
    public String reply;
    public String emotion;
    public Double confidence;
    public String roleType; // 可选：返回角色类型名
    public Long characterId; // 可选：返回角色ID
    public Long messageId;  // AI消息ID
    public Integer tokensUsed; // 本次对话消耗的Token数
}
