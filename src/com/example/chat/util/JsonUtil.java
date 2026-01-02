package com.example.chat.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {
    
    private static final Gson gson = new GsonBuilder().create();
    
    /**
     * 读取请求体内容
     */
    public static String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    /**
     * 将JSON字符串转换为对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    /**
     * 写入JSON响应（成功响应）
     */
    public static void writeJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.print(toJson(data));
            out.flush();
        }
    }
    
    /**
     * 写入JSON错误响应
     */
    public static void writeJsonResponse(HttpServletResponse response, String key, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"" + key + "\":\"" + message + "\"}");
            out.flush();
        }
    }
    
    /**
     * 写入JSON成功响应
     */
    public static void writeJsonSuccess(HttpServletResponse response, String message) throws IOException {
        writeJsonResponse(response, "success", message);
    }
    
    /**
     * 写入JSON错误响应
     */
    public static void writeJsonError(HttpServletResponse response, String message) throws IOException {
        writeJsonResponse(response, "error", message);
    }
}