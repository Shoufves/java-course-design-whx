package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.dao.UserLogDao;
import edu.gpnu.bigdata.entity.UserLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserLogDao mockDao;

    @InjectMocks
    private StatsService statsService;

    private List<UserLog> testLogs;

    @BeforeEach
    void setUp() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        testLogs = List.of(
                new UserLog(1L, 100L, "view", now, "app", "iPhone", "电子产品"),
                new UserLog(2L, 101L, "view", now, "web", "Windows", "服装服饰"),
                new UserLog(3L, 100L, "cart", now, "app", "iPhone", "电子产品")
        );
    }

    @Test
    void testCountByEventType() {
        when(mockDao.findAll()).thenReturn(testLogs);
        Map<String, Long> result = statsService.countByEventType();
        assertThat(result).containsEntry("view", 2L);
        assertThat(result).containsEntry("cart", 1L);
    }

    @Test
    void testCountByChannel() {
        when(mockDao.findAll()).thenReturn(testLogs);
        Map<String, Long> result = statsService.countByChannel();
        assertThat(result).containsEntry("app", 2L);
        assertThat(result).containsEntry("web", 1L);
    }

    @Test
    void testCountDailyPVUV() {
        when(mockDao.findAll()).thenReturn(testLogs);
        Map<LocalDate, Map<String, Long>> result = statsService.countDailyPVUV();
        assertThat(result).isNotEmpty();
    }

    @Test
    void testCalculateFunnel() {
        when(mockDao.findAll()).thenReturn(testLogs);
        Map<String, Object> result = statsService.calculateFunnel();
        @SuppressWarnings("unchecked")
        Map<String, Long> stepCounts = (Map<String, Long>) result.get("stepCounts");
        assertThat(stepCounts).containsEntry("view", 2L);
        assertThat(stepCounts).containsEntry("cart", 1L);
        assertThat(stepCounts).containsEntry("order", 0L);
        assertThat(stepCounts).containsEntry("pay", 0L);
    }

    @Test
    void testTopCategories() {
        when(mockDao.findAll()).thenReturn(testLogs);
        List<Map.Entry<String, Long>> result = statsService.topCategories(2);
        assertThat(result).hasSize(2);
    }

    @Test
    void testEmptyData() {
        when(mockDao.findAll()).thenReturn(List.of());
        Map<String, Long> result = statsService.countByEventType();
        assertThat(result).isEmpty();
    }
}
