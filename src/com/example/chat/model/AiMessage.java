package com.example.chat.model;

import java.sql.Timestamp;

public class AiMessage {
    private long id;
    private long sessionId;
    private int userId;
    private String role; // USER / ASSISTANT / SYSTEM
    private String content;
    private Timestamp createdAt;
    private Integer tokenCount;
    private boolean errorFlag;
    private String rawAiMetadata;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }

    public boolean isErrorFlag() { return errorFlag; }
    public void setErrorFlag(boolean errorFlag) { this.errorFlag = errorFlag; }

    public String getRawAiMetadata() { return rawAiMetadata; }
    public void setRawAiMetadata(String rawAiMetadata) { this.rawAiMetadata = rawAiMetadata; }
}

