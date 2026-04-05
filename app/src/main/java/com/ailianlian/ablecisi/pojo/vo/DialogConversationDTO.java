package com.ailianlian.ablecisi.pojo.vo;

/**
 * 与后端 {@code ConversationVO} 字段对齐，用于 Gson 反序列化。
 */
public class DialogConversationDTO {
    public Long id;
    public Long userId;
    public Long characterId;
    public String characterName;
    public String characterAvatar;
    public String title;
    public String lastMessage;
    public String lastMsgAt;
    public String updateTime;
}
