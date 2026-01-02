package com.example.chat.util;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AiConfigUtil {
    String key;
    String url;
    String model;

    public AiConfigUtil() throws ServletException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("ai.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new ServletException("Failed to load ai.properties", e);
        }
        this.key = props.getProperty("deepseek.apiKey");
        this.url = props.getProperty("deepseek.apiUrl");
        this.model =props.getProperty("deepseek.model");
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public String getModel() {
        return model;
    }
}
