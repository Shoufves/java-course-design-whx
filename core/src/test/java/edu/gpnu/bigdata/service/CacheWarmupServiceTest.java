package edu.gpnu.bigdata.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CacheWarmupService 集成测试（需要 MySQL + Redis 容器运行）
 */
class CacheWarmupServiceTest {

    @Test
    void testWarmupSync() {
        CacheWarmupService warmup = new CacheWarmupService();
        warmup.warmupSync();

        // 验证预热后 Redis 中有缓存数据
        CacheService cache = new CacheService();
        Map<String, String> eventTypeCache = cache.getCachedStats("eventType");
        assertThat(eventTypeCache).isNotEmpty();
        assertThat(eventTypeCache).containsKey("view");

        Map<String, String> channelCache = cache.getCachedStats("channel");
        assertThat(channelCache).isNotEmpty();
        assertThat(channelCache).containsKey("app");
    }
}
