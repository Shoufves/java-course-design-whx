package edu.gpnu.bigdata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportGenerator 集成测试（需要 MySQL 容器运行）
 */
class ReportGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void testGenerateReportCreatesFile() throws IOException {
        Path reportPath = tempDir.resolve("test-report.txt");
        ReportGenerator generator = new ReportGenerator();
        generator.generateReport(reportPath.toString());

        assertThat(reportPath).exists();
        String content = Files.readString(reportPath);
        assertThat(content).contains("用户行为日志统计分析报表");
        assertThat(content).contains("按事件类型统计");
        assertThat(content).contains("按渠道统计");
        assertThat(content).contains("漏斗转化率分析");
        assertThat(content).contains("商品类别 TOP5");
    }
}
