package edu.gpnu.bigdata.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("配置文件 application.properties 未找到，使用默认配置");
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/course_design?useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true");
                props.setProperty("db.username", "app_user");
                props.setProperty("db.password", "app_pass");
                props.setProperty("redis.host", "localhost");
                props.setProperty("redis.port", "6379");
            } else {
                props.load(input);
                logger.info("配置文件加载成功");
            }
        } catch (IOException e) {
            logger.error("加载配置文件失败", e);
            throw new RuntimeException("加载配置文件失败", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    // ★★★ main 方法：测试外部化配置加载 ★★★
    public static void main(String[] args) {
        System.out.println("===== 测试外部化配置加载 =====");
        System.out.println("db.url: " + ConfigLoader.get("db.url"));
        System.out.println("db.username: " + ConfigLoader.get("db.username"));
        System.out.println("db.password: " + ConfigLoader.get("db.password"));
        System.out.println("redis.host: " + ConfigLoader.get("redis.host"));
        System.out.println("redis.port: " + ConfigLoader.get("redis.port"));
        System.out.println("===== 测试完成 =====");
    }
}