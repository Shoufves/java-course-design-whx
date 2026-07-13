package edu.gpnu.bigdata.web;

import edu.gpnu.bigdata.dto.ApiResponse;
import edu.gpnu.bigdata.service.CacheService;
import edu.gpnu.bigdata.service.CacheWarmupService;
import edu.gpnu.bigdata.service.StatsService;
import edu.gpnu.bigdata.util.ConfigLoader;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * REST API服务器
 * 使用Javalin轻量级框架暴露统计数据的HTTP接口
 *
 * 学生只需要理解：
 * 1. 统计数据存在哪里（StatsService / Redis Cache）
 * 2. 启动 main 方法后访问 http://localhost:8080/api/stats 看到 JSON
 */
public class ApiServer {
    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);

    private final StatsService statsService;
    private final CacheService cacheService;

    public ApiServer() {
        this.statsService = new StatsService();
        this.cacheService = new CacheService();
    }

    public static void main(String[] args) {
        ApiServer server = new ApiServer();
        server.start();
    }

    public void start() {
        // 创建Javalin实例并启动
        var app = Javalin.create().start(8080);

        logger.info("🚀 API服务器已启动: http://localhost:8080");

        // 异步预热缓存（应用启动后后台自动加载数据到Redis）
        CacheWarmupService warmup = new CacheWarmupService();
        warmup.warmupAsync();
        logger.info("⏳ 缓存预热任务已提交（后台执行）");

        // ============================================================
        // 全局跨域支持（加分扩展仪表板需要）
        // ============================================================
        app.before(ctx -> ctx.header("Access-Control-Allow-Origin", "*"));

        // ============================================================
        // 路由定义（7个端点）
        // ============================================================

        // 路由1：综合统计
        // GET /api/stats → 返回事件类型 + 渠道聚合结果（优先返回缓存数据）
        app.get("/api/stats", ctx -> {
            logger.info("收到请求: GET /api/stats");

            Map<String, String> cachedEventType = cacheService.getCachedStats("eventType");
            Map<String, String> cachedChannel = cacheService.getCachedStats("channel");

            if (!cachedEventType.isEmpty() && !cachedChannel.isEmpty()) {
                logger.info("✅ 缓存命中，返回缓存数据");
                ctx.json(ApiResponse.success(Map.of(
                        "eventTypeStats", cachedEventType,
                        "channelStats", cachedChannel,
                        "fromCache", true
                )));
                return;
            }

            logger.info("⏳ 缓存未命中，执行计算...");
            Map<String, Long> eventStats = statsService.countByEventType();
            Map<String, Long> channelStats = statsService.countByChannel();

            cacheService.cacheStats("eventType", eventStats);
            cacheService.cacheStats("channel", channelStats);

            ctx.json(ApiResponse.success(Map.of(
                    "eventTypeStats", eventStats,
                    "channelStats", channelStats,
                    "fromCache", false
            )));
        });

        // 路由2：按事件类型统计
        // GET /api/stats/event-type → 返回 view/cart/order/pay 各事件数量
        app.get("/api/stats/event-type", ctx -> {
            logger.info("收到请求: GET /api/stats/event-type");

            Map<String, String> cached = cacheService.getCachedStats("eventType");
            if (!cached.isEmpty()) {
                ctx.json(ApiResponse.success(Map.of(
                        "data", cached,
                        "fromCache", true
                )));
                return;
            }

            Map<String, Long> result = statsService.countByEventType();
            cacheService.cacheStats("eventType", result);
            ctx.json(ApiResponse.success(Map.of(
                    "data", result,
                    "fromCache", false
            )));
        });

        // 路由3：按渠道统计
        // GET /api/stats/channel → 返回 app/web/miniprogram 各渠道数量
        app.get("/api/stats/channel", ctx -> {
            logger.info("收到请求: GET /api/stats/channel");

            Map<String, String> cached = cacheService.getCachedStats("channel");
            if (!cached.isEmpty()) {
                ctx.json(ApiResponse.success(Map.of(
                        "data", cached,
                        "fromCache", true
                )));
                return;
            }

            Map<String, Long> result = statsService.countByChannel();
            cacheService.cacheStats("channel", result);
            ctx.json(ApiResponse.success(Map.of(
                    "data", result,
                    "fromCache", false
            )));
        });

        // 路由4：漏斗转化率
        // GET /api/stats/funnel → 返回 view→cart→order→pay 各环节转化率
        app.get("/api/stats/funnel", ctx -> {
            logger.info("收到请求: GET /api/stats/funnel");

            try (Jedis jedis = new Jedis(
                    ConfigLoader.get("redis.host", "localhost"),
                    Integer.parseInt(ConfigLoader.get("redis.port", "6379"))
            )) {
                String cached = jedis.get("funnel:daily");
                if (cached != null && !cached.isEmpty()) {
                    logger.info("✅ 漏斗缓存命中");
                    ctx.json(ApiResponse.success(Map.of(
                            "data", cached,
                            "fromCache", true
                    )));
                    return;
                }
            } catch (Exception e) {
                logger.warn("读取漏斗缓存失败，将重新计算", e);
            }

            Map<String, Object> result = statsService.calculateFunnel();
            cacheService.cacheFunnel("daily", result);
            ctx.json(ApiResponse.success(Map.of(
                    "data", result,
                    "fromCache", false
            )));
        });

        // 路由5：商品类别TopN
        // GET /api/stats/top-categories?n=5 → 返回热门商品类别排名
        app.get("/api/stats/top-categories", ctx -> {
            int n = ctx.queryParam("n") != null
                    ? Integer.parseInt(ctx.queryParam("n"))
                    : 5;
            logger.info("收到请求: GET /api/stats/top-categories?n={}", n);

            Map<String, String> cached = cacheService.getCachedStats("topCategories");
            if (!cached.isEmpty()) {
                ctx.json(ApiResponse.success(Map.of(
                        "data", cached,
                        "fromCache", true
                )));
                return;
            }

            var result = statsService.topCategories(n);
            ctx.json(ApiResponse.success(Map.of(
                    "data", result,
                    "fromCache", false
            )));
        });

        // 路由6：健康检查
        // GET /api/health → 返回 OK
        app.get("/api/health", ctx -> ctx.result("OK"));

        // 路由7：清除缓存
        // DELETE /api/cache/{key} → 清除指定统计缓存
        app.delete("/api/cache/{key}", ctx -> {
            String key = ctx.pathParam("key");
            logger.info("收到请求: DELETE /api/cache/{}", key);
            cacheService.deleteCache(key);
            ctx.json(ApiResponse.success(Map.of(
                    "message", "缓存已清除: " + key
            )));
        });

        // 全局异常处理
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("请求处理异常: {}", ctx.url(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(ApiResponse.error(e.getMessage()));
        });
    }
}
