package edu.gpnu.bigdata.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StatsDTOTest {

    @Test
    void testEmptyStatsDTO() {
        StatsDTO dto = StatsDTO.empty();
        assertThat(dto.isEmpty()).isTrue();
        assertThat(dto.eventTypeStats()).isEmpty();
        assertThat(dto.channelStats()).isEmpty();
        assertThat(dto.dailyStats()).isEmpty();
        assertThat(dto.funnelResult()).isEmpty();
        assertThat(dto.topCategories()).isEmpty();
    }

    @Test
    void testNonEmptyStatsDTO() {
        StatsDTO dto = new StatsDTO(
                Map.of("view", 100L),
                Map.of("app", 50L),
                Map.of(),
                Map.of(),
                Map.of()
        );
        assertThat(dto.isEmpty()).isFalse();
        assertThat(dto.eventTypeStats()).containsEntry("view", 100L);
    }
}
