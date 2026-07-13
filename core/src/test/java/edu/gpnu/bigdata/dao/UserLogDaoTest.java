package edu.gpnu.bigdata.dao;

import edu.gpnu.bigdata.entity.UserLog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserLogDao 集成测试（需要 MySQL 容器运行在 localhost:3306，
 * 且 course_design.user_log 表中有数据）
 */
class UserLogDaoTest {

    private final UserLogDao dao = new UserLogDao();

    @Test
    void testFindAllReturnsData() {
        List<UserLog> logs = dao.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).id()).isNotNull();
        assertThat(logs.get(0).userId()).isNotNull();
        assertThat(logs.get(0).eventType()).isIn("view", "cart", "order", "pay");
    }
}
