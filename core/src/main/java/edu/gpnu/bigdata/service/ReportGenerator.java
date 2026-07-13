package edu.gpnu.bigdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表生成器
 * 将Stream API的统计结果生成为文本报表
 */
public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private final StatsService statsService = new StatsService();

    /**
     * 生成完整报表
     */
    public void generateReport(String filePath) {
        logger.info("开始生成报表: {}", filePath);

        StringBuilder report = new StringBuilder();

        // 报表头
        report.append("=".repeat(60)).append("\n");
        report.append("         用户行为日志统计分析报表\n");
        report.append("         生成时间: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        report.append("=".repeat(60)).append("\n\n");

        // 1. 按事件类型统计
        report.append("【一、按事件类型统计】\n");
        report.append("-".repeat(40)).append("\n");
        Map<String, Long> eventStats = statsService.countByEventType();
        long totalEvents = eventStats.values().stream().mapToLong(Long::longValue).sum();
        for (Map.Entry<String, Long> entry : eventStats.entrySet()) {
            double pct = (double) entry.getValue() / totalEvents * 100;
            report.append(String.format("  %-10s : %8d 条 (%.2f%%)\n",
                    entry.getKey(), entry.getValue(), pct));
        }
        report.append(String.format("  合计      : %8d 条\n\n", totalEvents));

        // 2. 按渠道统计
        report.append("【二、按渠道统计】\n");
        report.append("-".repeat(40)).append("\n");
        Map<String, Long> channelStats = statsService.countByChannel();
        for (Map.Entry<String, Long> entry : channelStats.entrySet()) {
            report.append(String.format("  %-12s : %8d 条\n",
                    entry.getKey(), entry.getValue()));
        }
        report.append("\n");

        // 3. 按天统计 PV/UV
        report.append("【三、按天统计 PV / UV】\n");
        report.append("-".repeat(40)).append("\n");
        report.append(String.format("  %-12s %10s %10s\n", "日期", "PV", "UV"));
        report.append("  " + "-".repeat(32)).append("\n");
        Map<LocalDate, Map<String, Long>> dailyStats = statsService.countDailyPVUV();
        for (Map.Entry<LocalDate, Map<String, Long>> entry : dailyStats.entrySet()) {
            Map<String, Long> stats = entry.getValue();
            report.append(String.format("  %-12s %10d %10d\n",
                    entry.getKey().toString(),
                    stats.getOrDefault("PV", 0L),
                    stats.getOrDefault("UV", 0L)));
        }
        report.append("\n");

        // 4. 漏斗转化率
        report.append("【四、漏斗转化率分析】\n");
        report.append("-".repeat(40)).append("\n");
        Map<String, Object> funnelResult = statsService.calculateFunnel();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rates = (List<Map<String, Object>>) funnelResult.get("conversionRates");
        for (Map<String, Object> rateInfo : rates) {
            report.append(String.format("  %-8s : %6d 人  (转化率: %s, 来自: %s)\n",
                    rateInfo.get("step"),
                    rateInfo.get("userCount"),
                    rateInfo.get("conversionRate"),
                    rateInfo.get("from")));
        }
        report.append("\n");

        // 5. Top5商品类别
        report.append("【五、商品类别 TOP5】\n");
        report.append("-".repeat(40)).append("\n");
        List<Map.Entry<String, Long>> topCategories = statsService.topCategories(5);
        int rank = 1;
        for (Map.Entry<String, Long> entry : topCategories) {
            report.append(String.format("  #%d  %-12s : %8d 条\n",
                    rank++, entry.getKey(), entry.getValue()));
        }
        report.append("\n");

        // 报表尾
        report.append("=".repeat(60)).append("\n");
        report.append("         报表生成完成\n");
        report.append("=".repeat(60)).append("\n");

        // 写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(report.toString());
            logger.info("报表已生成: {}", filePath);
        } catch (IOException e) {
            logger.error("写入报表文件失败", e);
            throw new RuntimeException("写入报表文件失败", e);
        }
    }

    public static void main(String[] args) {
        ReportGenerator generator = new ReportGenerator();
        generator.generateReport("report.txt");
        System.out.println("报表已生成，请查看项目根目录下的 report.txt");
    }
}
