package com.example.chat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DbUtil {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = DbUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties p = new Properties();
            if (in != null) {
                p.load(in);
                url = p.getProperty("jdbc.url");
                user = p.getProperty("jdbc.user");
                password = p.getProperty("jdbc.password");
                String driver = p.getProperty("jdbc.driver");
                if (driver != null && !driver.isEmpty()) Class.forName(driver);
            } else {
                System.err.println("db.properties not found on classpath");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

