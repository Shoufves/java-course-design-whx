package edu.gpnu.bigdata.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        ApiResponse<Map<String, Long>> resp = ApiResponse.success(Map.of("view", 100L));
        assertThat(resp.code()).isEqualTo(200);
        assertThat(resp.message()).isEqualTo("success");
        assertThat(resp.data()).containsEntry("view", 100L);
    }

    @Test
    void testSuccessWithoutData() {
        ApiResponse<Void> resp = ApiResponse.success();
        assertThat(resp.code()).isEqualTo(200);
        assertThat(resp.data()).isNull();
    }

    @Test
    void testError() {
        ApiResponse<Void> resp = ApiResponse.error("出错了");
        assertThat(resp.code()).isEqualTo(500);
        assertThat(resp.message()).isEqualTo("出错了");
        assertThat(resp.data()).isNull();
    }
}
