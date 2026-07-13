package edu.gpnu.bigdata.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(ConfigLoader.get("db.url"));
            config.setUsername(ConfigLoader.get("db.username"));
            config.setPassword(ConfigLoader.get("db.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setConnectionTestQuery("SELECT 1");
            dataSource = new HikariDataSource(config);
            logger.info("HikariCP 连接池初始化成功");
        } catch (Exception e) {
            logger.error("HikariCP 初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}