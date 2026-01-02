package com.example.chat.model;

import java.sql.Timestamp;

public class GroupMessage {
    private long messageId;
    private int groupId;
    private int senderId;
    private String content;
    private Timestamp sentAt;
    private String senderUsername;
    private boolean isCode;

    public long getMessageId() { return messageId; }
    public void setMessageId(long messageId) { this.messageId = messageId; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public boolean isCode() { return isCode; }
    public void setCode(boolean code) { isCode = code; }
}