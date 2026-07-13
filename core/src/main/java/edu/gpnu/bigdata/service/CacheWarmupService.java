package edu.gpnu.bigdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热服务
 * 使用CompletableFuture在应用启动时异步加载热门数据到Redis
 */
public class CacheWarmupService {
    private static final Logger logger = LoggerFactory.getLogger(CacheWarmupService.class);

    private final StatsService statsService;
    private final CacheService cacheService;

    public CacheWarmupService() {
        this.statsService = new StatsService();
        this.cacheService = new CacheService();
    }

    /**
     * 异步预热所有缓存
     * 使用CompletableFuture.runAsync()在后台线程执行
     * 不阻塞主线程，应用可以正常启动
     */
    public void warmupAsync() {
        logger.info("开始异步预热缓存...");

        // 使用CompletableFuture在后台执行预热任务
        CompletableFuture<Void> warmupTask = CompletableFuture.runAsync(() -> {
            try {
                logger.info("预热任务在后台线程启动");

                // 预热1：按事件类型统计
                Map<String, Long> eventStats = statsService.countByEventType();
                cacheService.cacheStats("eventType", eventStats);
                logger.info("✅ 事件类型统计已预热");

                // 预热2：按渠道统计
                Map<String, Long> channelStats = statsService.countByChannel();
                cacheService.cacheStats("channel", channelStats);
                logger.info("✅ 渠道统计已预热");

                // 预热3：漏斗转化率
                Map<String, Object> funnelResult = statsService.calculateFunnel();
                cacheService.cacheFunnel("daily", funnelResult);
                logger.info("✅ 漏斗转化率已预热");

                // 预热4：商品类别Top10
                Map<String, Long> topCategories = statsService.topCategories(10).stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (v1, v2) -> v1,
                                java.util.LinkedHashMap::new
                        ));
                cacheService.cacheStats("topCategories", topCategories);
                logger.info("✅ 商品类别Top10已预热");

                logger.info("🎯 缓存预热完成！所有统计数据已加载到Redis");

            } catch (Exception e) {
                logger.error("❌ 缓存预热失败", e);
            }
        });

        // 可选：设置超时时间，防止预热任务卡住
        warmupTask.orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    logger.warn("缓存预热超时（30秒），应用继续启动", throwable);
                    return null;
                });

        logger.info("异步预热任务已提交，主线程继续执行");
    }

    /**
     * 同步预热（用于测试或调试）
     */
    public void warmupSync() {
        logger.info("开始同步预热缓存...");
        Map<String, Long> eventStats = statsService.countByEventType();
        cacheService.cacheStats("eventType", eventStats);
        cacheService.cacheStats("channel", statsService.countByChannel());
        cacheService.cacheFunnel("daily", statsService.calculateFunnel());
        logger.info("同步预热完成");
    }

    /**
     * 测试方法：验证异步预热是否正常工作
     */
    public static void main(String[] args) throws InterruptedException {
        CacheWarmupService warmup = new CacheWarmupService();

        System.out.println("===== 测试异步预热 =====");
        warmup.warmupAsync();

        // 等待预热完成（给后台线程一些时间）
        System.out.println("等待预热完成...");
        Thread.sleep(5000);

        // 验证缓存是否写入成功
        CacheService cache = new CacheService();
        Map<String, String> cached = cache.getCachedStats("eventType");
        System.out.println("缓存中的事件类型统计: " + cached);

        System.out.println("===== 测试完成 =====");
    }
}
