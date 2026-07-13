package edu.gpnu.bigdata;

import edu.gpnu.bigdata.service.CacheService;
import edu.gpnu.bigdata.service.ReportGenerator;
import edu.gpnu.bigdata.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 统计分析集成测试
 * 验证Stream API聚合 + 报表生成 + Redis缓存
 */
public class StatsIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(StatsIntegrationTest.class);

    public static void main(String[] args) {
        System.out.println("===== 统计分析集成测试开始 =====");

        StatsService stats = new StatsService();
        CacheService cache = new CacheService();

        // 1. 测试Stream API聚合
        System.out.println("\n--- 1. 按事件类型统计 ---");
        Map<String, Long> eventStats = stats.countByEventType();
        eventStats.forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // 2. 测试缓存写入
        System.out.println("\n--- 2. 写入Redis缓存 ---");
        cache.cacheStats("eventType", eventStats);

        // 3. 测试缓存读取
        System.out.println("\n--- 3. 从Redis读取缓存 ---");
        Map<String, String> cached = cache.getCachedStats("eventType");
        cached.forEach((k, v) -> System.out.println("  缓存: " + k + " = " + v));

        // 4. 测试漏斗分析
        System.out.println("\n--- 4. 漏斗转化率分析 ---");
        Map<String, Object> funnel = stats.calculateFunnel();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rates = (List<Map<String, Object>>) funnel.get("conversionRates");
        for (Map<String, Object> r : rates) {
            System.out.println("  " + r.get("step") + ": " + r.get("userCount") +
                    " 人, 转化率: " + r.get("conversionRate"));
        }

        // 5. 测试报表生成
        System.out.println("\n--- 5. 生成报表 ---");
        ReportGenerator generator = new ReportGenerator();
        generator.generateReport("report.txt");
        System.out.println("  报表已生成: report.txt");

        // 6. 测试Top5商品类别
        System.out.println("\n--- 6. 商品类别Top5 ---");
        List<Map.Entry<String, Long>> top = stats.topCategories(5);
        int rank = 1;
        for (Map.Entry<String, Long> e : top) {
            System.out.println("  #" + rank++ + ": " + e.getKey() + " (" + e.getValue() + "条)");
        }

        System.out.println("\n===== 集成测试完成 =====");
    }
}
