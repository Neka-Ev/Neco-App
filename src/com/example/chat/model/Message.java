package com.example.chat.model;

import java.sql.Timestamp;

public class Message {
    private long id;
    private int senderId;
    private Integer receiverId; // null for public messages
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private String messageType; // 'public' or 'private'
    private boolean isRead;
    private Timestamp createdAt;
    // 标记是否为代码消息
    private boolean isCode;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public boolean isCode() { return isCode; }
    public void setCode(boolean code) { isCode = code; }
}