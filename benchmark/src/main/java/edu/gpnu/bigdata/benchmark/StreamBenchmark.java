package edu.gpnu.bigdata.benchmark;

import edu.gpnu.bigdata.service.StatsService;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH基准测试：对比顺序流、并行流与自定义收集器的性能差异
 *
 * 运行方式：直接运行main方法
 *
 * 关键注解说明：
 * - @Benchmark: 标记被测试的方法
 * - @State(Scope.Thread): 每个线程独立实例
 * - @Warmup: 预热迭代次数
 * - @Measurement: 测量迭代次数
 * - @BenchmarkMode: 测试模式（吞吐量/平均时间等）
 * - @OutputTimeUnit: 结果时间单位
 */
@BenchmarkMode(Mode.Throughput)          // 测试模式：吞吐量（每秒操作数）
@OutputTimeUnit(TimeUnit.SECONDS)        // 结果单位：秒
@State(Scope.Thread)                     // 每个线程独立实例
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)  // 预热3次
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 测量5次
@Fork(1)                                 // 启动1个独立的JVM进程
public class StreamBenchmark {

    private StatsService statsService;

    /**
     * 初始化方法：在每个测试迭代前执行
     */
    @Setup
    public void setup() {
        statsService = new StatsService();
    }

    /**
     * 测试1：顺序流按事件类型统计
     */
    @Benchmark
    public Map<String, Long> testSequentialStream() {
        return statsService.countByEventType();
    }

    /**
     * 测试2：并行流按事件类型统计
     */
    @Benchmark
    public Map<String, Long> testParallelStream() {
        return statsService.countByEventTypeParallel();
    }

    /**
     * 测试3：使用自定义FunnelCollector
     */
    @Benchmark
    public Map<String, Long> testCustomCollector() {
        return statsService.countFunnelWithCustomCollector();
    }

    /**
     * 主方法：运行基准测试
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StreamBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
