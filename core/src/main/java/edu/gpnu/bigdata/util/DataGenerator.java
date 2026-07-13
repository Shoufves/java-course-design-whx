package edu.gpnu.bigdata.util;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * 数据生成器
 * 1. 先生成 10,000 个用户（插入 user 表）
 * 2. 再生成 100,000 条用户行为日志（user_id 从已存在的用户中随机选取）
 * 通过 JDBC 批量插入 MySQL，配合 rewriteBatchedStatements=true 大幅提升性能
 */
public class DataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

    // 配置参数
    private static final int TOTAL_USERS = 10_000;          // 用户数
    private static final int TOTAL_RECORDS = 100_000;       // 总记录数：10万
    private static final int BATCH_SIZE = 1000;             // 每批插入1000条
    private static final String[] EVENT_TYPES = {"view", "cart", "order", "pay"};
    private static final String[] CHANNELS = {"app", "web", "miniprogram"};
    private static final String[] DEVICES = {"iPhone", "Android", "Windows", "macOS"};
    private static final String[] CATEGORIES = {"电子产品", "服装服饰", "食品饮料", "家居用品", "图书音像"};

    public static void main(String[] args) {
        logger.info("========== 开始生成测试数据 ==========");
        logger.info("目标用户数: {} 个", TOTAL_USERS);
        logger.info("目标日志记录数: {} 条", TOTAL_RECORDS);
        logger.info("批量大小: {} 条/批", BATCH_SIZE);

        long startTime = System.currentTimeMillis();

        Faker faker = new Faker(java.util.Locale.CHINA);

        try (Connection conn = DataSourceFactory.getConnection()) {
            // ---- 第1步：生成用户 ----
            generateUsers(conn);

            // ---- 第2步：生成行为日志 ----
            generateLogs(conn, faker);

            long endTime = System.currentTimeMillis();
            logger.info("========== 全部数据生成完成 ==========");
            logger.info("总耗时: {} 秒", (endTime - startTime) / 1000);

        } catch (Exception e) {
            logger.error("数据生成失败", e);
        }
    }

    /**
     * 生成用户数据，插入 user 表
     */
    private static void generateUsers(Connection conn) throws SQLException {
        logger.info("--- 开始生成用户数据 ---");

        String sql = "INSERT INTO user (username, created_at) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            int inserted = 0;
            for (int i = 1; i <= TOTAL_USERS; i++) {
                // 使用 user_1, user_2, ... 保证唯一
                String username = "user_" + i;
                Timestamp now = new Timestamp(System.currentTimeMillis());

                pstmt.setString(1, username);
                pstmt.setTimestamp(2, now);
                pstmt.addBatch();

                if (i % BATCH_SIZE == 0) {
                    int[] result = pstmt.executeBatch();
                    inserted += result.length;
                    conn.commit();
                    logger.info("已插入 {} / {} 个用户", inserted, TOTAL_USERS);
                    pstmt.clearBatch();
                }
            }
            // 处理最后一批
            int[] result = pstmt.executeBatch();
            inserted += result.length;
            conn.commit();
            logger.info("用户数据生成完成，共插入 {} 个用户", inserted);
        } catch (SQLException e) {
            conn.rollback();
            logger.error("用户数据生成失败", e);
            throw e;
        }
    }

    /**
     * 生成行为日志数据，user_id 从已有用户中随机选取
     */
    private static void generateLogs(Connection conn, Faker faker) throws SQLException {
        logger.info("--- 开始生成行为日志数据 ---");

        String sql = "INSERT INTO user_log (user_id, event_type, event_time, channel, device, product_category) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            int totalInserted = 0;

            for (int i = 0; i < TOTAL_RECORDS; i++) {
                // 随机 user_id 范围 1 ~ TOTAL_USERS
                long userId = faker.number().numberBetween(1L, (long) TOTAL_USERS + 1);
                String eventType = EVENT_TYPES[faker.number().numberBetween(0, EVENT_TYPES.length)];
                Timestamp eventTime = generateRandomTimestamp(faker);
                String channel = CHANNELS[faker.number().numberBetween(0, CHANNELS.length)];
                String device = DEVICES[faker.number().numberBetween(0, DEVICES.length)];
                String category = CATEGORIES[faker.number().numberBetween(0, CATEGORIES.length)];

                pstmt.setLong(1, userId);
                pstmt.setString(2, eventType);
                pstmt.setTimestamp(3, eventTime);
                pstmt.setString(4, channel);
                pstmt.setString(5, device);
                pstmt.setString(6, category);

                pstmt.addBatch();

                if ((i + 1) % BATCH_SIZE == 0) {
                    int[] result = pstmt.executeBatch();
                    totalInserted += result.length;
                    conn.commit();
                    logger.info("已插入 {} / {} 条日志", totalInserted, TOTAL_RECORDS);
                    pstmt.clearBatch();
                }
            }

            // 处理最后一批
            int[] result = pstmt.executeBatch();
            totalInserted += result.length;
            conn.commit();

            logger.info("行为日志数据生成完成，共插入 {} 条记录", totalInserted);
        } catch (SQLException e) {
            conn.rollback();
            logger.error("日志数据生成失败", e);
            throw e;
        }
    }

    /**
     * 生成随机的历史时间戳（过去30天内）
     */
    private static Timestamp generateRandomTimestamp(Faker faker) {
        java.util.Date pastDate = faker.date().past(30, TimeUnit.DAYS);
        return new Timestamp(pastDate.getTime());
    }
}
