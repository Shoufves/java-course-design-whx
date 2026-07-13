package edu.gpnu.bigdata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gpnu.bigdata.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Redis缓存服务
 * 使用Jedis将聚合结果缓存到Redis
 */
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    // 缓存过期时间：1小时（3600秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600;

    // Jedis连接池（单例模式）
    private static final JedisPool pool;

    static {
        String host = ConfigLoader.get("redis.host", "localhost");
        int port = Integer.parseInt(ConfigLoader.get("redis.port", "6379"));

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);          // 最大连接数
        config.setMaxIdle(5);            // 最大空闲连接数
        config.setMinIdle(1);            // 最小空闲连接数
        config.setTestOnBorrow(true);    // 借用连接时测试是否可用

        pool = new JedisPool(config, host, port, 2000);  // 超时时间2秒
        logger.info("Redis连接池初始化成功: {}:{}", host, port);
    }

    /**
     * 将统计数据缓存到Redis Hash结构中
     * Key格式: stats:{统计类型}
     * 例如: stats:eventType, stats:channel, stats:daily
     */
    public void cacheStats(String key, Map<String, ? extends Number> stats) {
        try (Jedis jedis = pool.getResource()) {
            String cacheKey = "stats:" + key;

            // 将Map转换为Hash存储
            for (Map.Entry<String, ? extends Number> entry : stats.entrySet()) {
                jedis.hset(cacheKey, entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 设置过期时间
            jedis.expire(cacheKey, CACHE_EXPIRE_SECONDS);
            logger.info("缓存写入成功: {}, 共 {} 个字段", cacheKey, stats.size());

        } catch (Exception e) {
            logger.error("缓存写入失败: {}", key, e);
        }
    }

    /**
     * 从Redis读取缓存
     */
    public Map<String, String> getCachedStats(String key) {
        try (Jedis jedis = pool.getResource()) {
            String cacheKey = "stats:" + key;
            Map<String, String> result = jedis.hgetAll(cacheKey);

            if (result.isEmpty()) {
                logger.info("缓存未命中: {}", cacheKey);
            } else {
                logger.info("缓存命中: {}, 共 {} 个字段", cacheKey, result.size());
            }

            return result;

        } catch (Exception e) {
            logger.error("读取缓存失败: {}", key, e);
            return Map.of();
        }
    }

    /**
     * 缓存漏斗分析结果（使用JSON序列化）
     */
    public void cacheFunnel(String key, Object funnelData) {
        try (Jedis jedis = pool.getResource()) {
            String cacheKey = "funnel:" + key;
            String json = mapper.writeValueAsString(funnelData);
            jedis.set(cacheKey, json);
            jedis.expire(cacheKey, CACHE_EXPIRE_SECONDS);
            logger.info("漏斗缓存写入成功: {}", cacheKey);

        } catch (JsonProcessingException e) {
            logger.error("漏斗数据序列化失败", e);
        } catch (Exception e) {
            logger.error("漏斗缓存写入失败", e);
        }
    }

    /**
     * 删除指定缓存
     */
    public void deleteCache(String key) {
        try (Jedis jedis = pool.getResource()) {
            String cacheKey = "stats:" + key;
            jedis.del(cacheKey);
            logger.info("缓存已删除: {}", cacheKey);
        }
    }

    /**
     * 测试Redis连接
     */
    public static void testConnection() {
        try (Jedis jedis = pool.getResource()) {
            String pong = jedis.ping();
            logger.info("Redis连接测试: {}", pong);
            System.out.println("Redis连接成功! PING响应: " + pong);
        } catch (Exception e) {
            logger.error("Redis连接失败", e);
            System.out.println("Redis连接失败: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 测试Redis连接
        testConnection();

        // 测试缓存读写
        CacheService cache = new CacheService();
        Map<String, Long> testData = Map.of("view", 10000L, "cart", 4000L);
        cache.cacheStats("test", testData);

        Map<String, String> cached = cache.getCachedStats("test");
        System.out.println("读取缓存: " + cached);
    }
}
