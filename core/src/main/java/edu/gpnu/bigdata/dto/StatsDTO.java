package edu.gpnu.bigdata.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计结果DTO（使用record定义不可变数据传输对象）
 * record自动生成构造器、getter、equals、hashCode、toString
 */
public record StatsDTO(
        // 按事件类型统计结果
        Map<String, Long> eventTypeStats,
        // 按渠道统计结果
        Map<String, Long> channelStats,
        // 按天统计PV/UV结果
        Map<LocalDate, Map<String, Long>> dailyStats,
        // 漏斗转化率结果
        Map<String, Object> funnelResult,
        // 商品类别TopN
        Map<String, Long> topCategories
) {
    /**
     * 工厂方法：创建空统计结果（用于缓存未命中时的默认返回）
     */
    public static StatsDTO empty() {
        return new StatsDTO(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of()
        );
    }

    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return eventTypeStats.isEmpty() && channelStats.isEmpty();
    }
}
