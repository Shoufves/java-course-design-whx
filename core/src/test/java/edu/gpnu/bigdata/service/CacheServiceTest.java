package edu.gpnu.bigdata.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CacheService 集成测试（需要 Redis 容器运行在 localhost:6379）
 */
class CacheServiceTest {

    private final CacheService cacheService = new CacheService();

    @Test
    void testCacheWriteAndRead() {
        Map<String, Long> testData = Map.of("view", 100L, "cart", 50L);
        cacheService.cacheStats("test_" + System.currentTimeMillis(), testData);
    }

    @Test
    void testGetNonExistentCache() {
        Map<String, String> result = cacheService.getCachedStats("nonexistent_" + System.currentTimeMillis());
        assertThat(result).isEmpty();
    }

    @Test
    void testDeleteCache() {
        String key = "del_test_" + System.currentTimeMillis();
        cacheService.cacheStats(key, Map.of("a", 1L));
        cacheService.deleteCache(key);
        Map<String, String> result = cacheService.getCachedStats(key);
        assertThat(result).isEmpty();
    }
}
