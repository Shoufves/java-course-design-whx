package edu.gpnu.bigdata.collector;

import edu.gpnu.bigdata.entity.UserLog;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FunnelCollectorTest {

    private final Timestamp now = new Timestamp(System.currentTimeMillis());

    @Test
    void testFunnelCollectorBasic() {
        List<UserLog> logs = List.of(
                new UserLog(1L, 100L, "view", now, "app", "iPhone", "电子产品"),
                new UserLog(2L, 101L, "view", now, "web", "Windows", "服装服饰"),
                new UserLog(3L, 100L, "cart", now, "app", "iPhone", "电子产品"),
                new UserLog(4L, 101L, "cart", now, "web", "Windows", "服装服饰"),
                new UserLog(5L, 100L, "order", now, "app", "iPhone", "电子产品"),
                new UserLog(6L, 100L, "pay", now, "app", "iPhone", "电子产品")
        );

        Map<String, Long> result = logs.stream().collect(FunnelCollector.toFunnel());
        assertThat(result).containsEntry("view", 2L);
        assertThat(result).containsEntry("cart", 2L);
        assertThat(result).containsEntry("order", 1L);
        assertThat(result).containsEntry("pay", 1L);
    }

    @Test
    void testFunnelCollectorEmpty() {
        List<UserLog> logs = List.of();
        Map<String, Long> result = logs.stream().collect(FunnelCollector.toFunnel());
        assertThat(result).containsEntry("view", 0L);
        assertThat(result).containsEntry("cart", 0L);
        assertThat(result).containsEntry("order", 0L);
        assertThat(result).containsEntry("pay", 0L);
    }

    @Test
    void testFunnelCollectorOrderPreserved() {
        List<UserLog> logs = List.of(
                new UserLog(1L, 100L, "pay", now, "app", "iPhone", "电子产品"),
                new UserLog(2L, 100L, "view", now, "app", "iPhone", "电子产品")
        );
        Map<String, Long> result = logs.stream().collect(FunnelCollector.toFunnel());
        // 验证结果按 view/cart/order/pay 顺序输出
        assertThat(result.keySet()).containsSequence("view", "cart", "order", "pay");
    }

    @Test
    void testFunnelAccumulatorMerge() {
        FunnelCollector.FunnelAccumulator acc1 = new FunnelCollector.FunnelAccumulator();
        acc1.add(new UserLog(1L, 100L, "view", now, "app", "iPhone", "电子产品"));

        FunnelCollector.FunnelAccumulator acc2 = new FunnelCollector.FunnelAccumulator();
        acc2.add(new UserLog(2L, 101L, "view", now, "web", "Windows", "服装服饰"));
        acc2.add(new UserLog(3L, 101L, "cart", now, "web", "Windows", "服装服饰"));

        acc1.merge(acc2);
        Map<String, Long> result = acc1.getStepCounts();
        assertThat(result).containsEntry("view", 2L);
        assertThat(result).containsEntry("cart", 1L);
    }
}
