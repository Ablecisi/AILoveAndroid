package com.ailianlian.ablecisi.pojo.entity;

import java.time.LocalDateTime;

/**
 * ailianlian
 * com.ailianlian.ablecisi.pojo
 * Message <br>
 * 消息类
 * @author Ablecisi
 * @version 1.0
 * 2025/4/18
 * 星期五
 * 16:05
 */
public class Message {
    public static final Integer TYPE_SENT = 0; // 发送消息
    public static final Integer TYPE_RECEIVED = 1; // 接收消息

    public String id; // 消息ID
    public String content; // 消息内容
    public LocalDateTime timestamp; // 消息时间戳
    public Integer type; // 0: 发送, 1: 接收
    public Boolean isRead; // 是否已读
    public String senderId; // 发送者ID
    public String receiverId; // 接收者ID

    public Message() {
    }

    public Message(String id, String content, LocalDateTime timestamp, Integer type, Boolean isRead, String senderId, String receiverId) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.isRead = isRead;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}
