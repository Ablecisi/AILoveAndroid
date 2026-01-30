package com.ailianlian.ablecisi.pojo.entity;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo
 * ChatSession <br>
 * 聊天会话类
 * @author Ablecisi
 * @version 1.0
 * 2025/4/18
 * 星期五
 * 16:07
 */
public class Conversation {
    private String id;
    private String characterId; // 角色ID， 用于唯一标识聊天会话中的角色
    private String characterName; // 角色名称
    private String characterAvatar; // 角色头像URL
    private String lastMessage; // 最后消息内容
    private String lastTime; // 最后消息时间，格式为 "yyyy-MM-dd HH:mm:ss"
    private Integer unreadCount; // 未读消息数量
    private Boolean online; // 角色是否在线

    public Conversation(String id, String characterId, String characterName, String characterAvatar,
                        String lastMessage, String lastTime, Integer unreadCount, Boolean online) {
        this.id = id;
        this.characterId = characterId;
        this.characterName = characterName;
        this.characterAvatar = characterAvatar;
        this.lastMessage = lastMessage;
        this.lastTime = lastTime;
        this.unreadCount = unreadCount;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getCharacterAvatar() {
        return characterAvatar;
    }

    public void setCharacterAvatar(String characterAvatar) {
        this.characterAvatar = characterAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
