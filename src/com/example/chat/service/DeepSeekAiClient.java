package com.example.chat.service;

import com.example.chat.model.AiMessage;
import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DeepSeekAiClient implements AiClient {

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final Gson gson = new Gson();

    public DeepSeekAiClient(String apiKey, String apiUrl, String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl != null ? apiUrl : "https://api.deepseek.com/v1/chat/completions";
        this.model = model != null ? model : "deepseek-chat";
    }

    @Override
    public String chat(List<AiMessage> contextMessages, String userInput) throws Exception {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new IllegalArgumentException("userInput is empty");
        }

        // 1. 组装 messages
        List<JsonObject> messagesJson = new ArrayList<>();

        // 可选：系统提示词
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "你的名字是Neco,一名 Web 高级工程师，以工程视角解决技术问题，提供可落地方案，讲清原理与设计原因，分析代码执行流程，用中文进行纯文本聊天式段落输出，说话风趣，可附带emoji");
        messagesJson.add(systemMsg);

        if (contextMessages != null) {
            for (AiMessage m : contextMessages) {
                String role;
                if ("USER".equalsIgnoreCase(m.getRole())) {
                    role = "user";
                } else if ("ASSISTANT".equalsIgnoreCase(m.getRole())) {
                    role = "assistant";
                } else if ("SYSTEM".equalsIgnoreCase(m.getRole())) {
                    role = "system";
                } else {
                    continue; // 未知角色直接跳过
                }
                JsonObject msg = new JsonObject();
                msg.addProperty("role", role);
                msg.addProperty("content", m.getContent());
                messagesJson.add(msg);
            }
        }

        // 本次用户输入追加到最后
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userInput);
        messagesJson.add(userMsg);

        // 2. 构造请求 JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        JsonArray messagesArr = new JsonArray();
        for (JsonObject m : messagesJson) {
            messagesArr.add(m);
        }
        payload.add("messages", messagesArr);

        String requestBody = gson.toJson(payload);

        // 3. 发送 HTTP 请求
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            String responseBody = sb.toString();

            if (status < 200 || status >= 300) {
                throw new RuntimeException("DeepSeek API error: HTTP " + status + ", body=" + responseBody);
            }

            // 4. 解析响应 JSON，取出 AI 回复内容
            JsonObject respJson = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = respJson.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                throw new RuntimeException("DeepSeek API returned no choices");
            }
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            if (message == null || !message.has("content")) {
                throw new RuntimeException("DeepSeek API: missing message.content");
            }
            return message.get("content").getAsString();

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
