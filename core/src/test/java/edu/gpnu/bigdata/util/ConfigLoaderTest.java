package edu.gpnu.bigdata.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigLoaderTest {

    @Test
    void testGetExistingKey() {
        String url = ConfigLoader.get("db.url");
        assertThat(url).isNotNull().contains("mysql");
    }

    @Test
    void testGetNonExistingKey() {
        String value = ConfigLoader.get("nonexistent.key");
        assertThat(value).isNull();
    }

    @Test
    void testGetWithDefault() {
        String value = ConfigLoader.get("nonexistent.key", "default_value");
        assertThat(value).isEqualTo("default_value");
    }

    @Test
    void testRedisConfigLoaded() {
        assertThat(ConfigLoader.get("redis.host")).isEqualTo("localhost");
    }
}
