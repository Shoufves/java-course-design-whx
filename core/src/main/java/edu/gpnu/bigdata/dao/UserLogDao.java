package edu.gpnu.bigdata.dao;

import edu.gpnu.bigdata.entity.UserLog;
import edu.gpnu.bigdata.util.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户行为日志数据访问层
 * 负责从数据库读取数据
 */
public class UserLogDao {
    private static final Logger logger = LoggerFactory.getLogger(UserLogDao.class);

    /**
     * 查询所有用户行为日志
     * 返回List<UserLog>供Stream API处理
     */
    public List<UserLog> findAll() {
        List<UserLog> logs = new ArrayList<>();
        String sql = "SELECT id, user_id, event_type, event_time, channel, device, product_category " +
                "FROM user_log ORDER BY id";

        logger.info("开始查询数据库...");

        try (Connection conn = DataSourceFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserLog log = new UserLog(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("event_type"),
                        rs.getTimestamp("event_time"),
                        rs.getString("channel"),
                        rs.getString("device"),
                        rs.getString("product_category")
                );
                logs.add(log);
            }

            logger.info("查询完成，共读取 {} 条记录", logs.size());
            return logs;

        } catch (SQLException e) {
            logger.error("查询数据失败", e);
            throw new RuntimeException("查询数据失败", e);
        }
    }

    /**
     * 查询指定日期范围的数据
     */
    public List<UserLog> findByDateRange(Timestamp start, Timestamp end) {
        // 实现略，可作为扩展练习
        return findAll();
    }
}
