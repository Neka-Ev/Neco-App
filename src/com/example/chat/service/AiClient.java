package com.example.chat.service;

import com.example.chat.model.AiMessage;

import java.util.List;

public interface AiClient {
    String chat(List<AiMessage> contextMessages, String userInput) throws Exception;
}

