package com.example.chat.model;

import java.sql.Timestamp;

public class ChatSession {
    private long id;
    private int user1Id;
    private int user2Id;
    private String user1Username;
    private String user2Username;
    private Long lastMessageId;
    private int unreadCountUser1;
    private int unreadCountUser2;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public ChatSession() {}
    
    public ChatSession(long id, int user1Id, int user2Id, String user1Username, String user2Username, 
                      Long lastMessageId, int unreadCountUser1, int unreadCountUser2, 
                      Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.user1Username = user1Username;
        this.user2Username = user2Username;
        this.lastMessageId = lastMessageId;
        this.unreadCountUser1 = unreadCountUser1;
        this.unreadCountUser2 = unreadCountUser2;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public int getUser1Id() { return user1Id; }
    public void setUser1Id(int user1Id) { this.user1Id = user1Id; }
    
    public int getUser2Id() { return user2Id; }
    public void setUser2Id(int user2Id) { this.user2Id = user2Id; }
    
    public String getUser1Username() { return user1Username; }
    public void setUser1Username(String user1Username) { this.user1Username = user1Username; }
    
    public String getUser2Username() { return user2Username; }
    public void setUser2Username(String user2Username) { this.user2Username = user2Username; }
    
    public Long getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(Long lastMessageId) { this.lastMessageId = lastMessageId; }
    
    public int getUnreadCountUser1() { return unreadCountUser1; }
    public void setUnreadCountUser1(int unreadCountUser1) { this.unreadCountUser1 = unreadCountUser1; }
    
    public int getUnreadCountUser2() { return unreadCountUser2; }
    public void setUnreadCountUser2(int unreadCountUser2) { this.unreadCountUser2 = unreadCountUser2; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}