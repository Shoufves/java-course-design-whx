package edu.gpnu.bigdata.collector;

import edu.gpnu.bigdata.entity.UserLog;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * 自定义收集器：在一次遍历中完成漏斗各环节的独立用户统计
 *
 * 使用场景：Stream<UserLog> → 统计view/cart/order/pay各环节的独立用户数
 *
 * Collector<T, A, R> 类型参数：
 * - T: UserLog（流中元素类型）
 * - A: FunnelAccumulator（中间累加器类型）
 * - R: Map<String, Long>（最终结果类型）
 */
public class FunnelCollector implements Collector<UserLog, FunnelCollector.FunnelAccumulator, Map<String, Long>> {

    // 漏斗环节顺序（按业务流转顺序）
    private static final List<String> STEP_ORDER = List.of("view", "cart", "order", "pay");

    /**
     * 中间累加器（容器）
     * 使用Map<String, Set<Long>>存储每个环节的独立用户ID集合
     */
    public static class FunnelAccumulator {
        // key: 事件类型 (view/cart/order/pay)
        // value: 该事件对应的独立用户ID集合
        private final Map<String, Set<Long>> eventUserSets = new HashMap<>();

        /**
         * 添加一个用户日志到累加器
         */
        public void add(UserLog log) {
            String eventType = log.eventType();
            Long userId = log.userId();

            // 如果该事件类型还没有对应的Set，创建一个新的HashSet
            eventUserSets.computeIfAbsent(eventType, k -> new HashSet<>())
                    .add(userId);
        }

        /**
         * 合并另一个累加器（用于并行流）
         */
        public void merge(FunnelAccumulator other) {
            for (Map.Entry<String, Set<Long>> entry : other.eventUserSets.entrySet()) {
                String eventType = entry.getKey();
                Set<Long> otherUserIds = entry.getValue();

                eventUserSets.computeIfAbsent(eventType, k -> new HashSet<>())
                        .addAll(otherUserIds);
            }
        }

        /**
         * 获取各环节的用户数（按漏斗顺序）
         */
        public Map<String, Long> getStepCounts() {
            Map<String, Long> result = new LinkedHashMap<>();
            for (String step : STEP_ORDER) {
                Set<Long> users = eventUserSets.getOrDefault(step, Collections.emptySet());
                result.put(step, (long) users.size());
            }
            return result;
        }
    }

    // ========== 实现Collector接口的五个方法 ==========

    /**
     * 1. supplier(): 创建新的空累加器
     */
    @Override
    public Supplier<FunnelAccumulator> supplier() {
        return FunnelAccumulator::new;
    }

    /**
     * 2. accumulator(): 将元素添加到累加器
     */
    @Override
    public BiConsumer<FunnelAccumulator, UserLog> accumulator() {
        return FunnelAccumulator::add;
    }

    /**
     * 3. combiner(): 合并两个累加器（并行流使用）
     */
    @Override
    public BinaryOperator<FunnelAccumulator> combiner() {
        return (acc1, acc2) -> {
            acc1.merge(acc2);
            return acc1;
        };
    }

    /**
     * 4. finisher(): 将累加器转换为最终结果
     */
    @Override
    public Function<FunnelAccumulator, Map<String, Long>> finisher() {
        return FunnelAccumulator::getStepCounts;
    }

    /**
     * 5. characteristics(): 返回收集器的特性
     * - UNORDERED: 流元素顺序不影响结果
     * （不加 CONCURRENT，因为累加器使用HashMap/HashSet非线程安全）
     */
    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(
                Characteristics.UNORDERED
        ));
    }

    /**
     * 静态工厂方法：更方便地创建FunnelCollector实例
     */
    public static FunnelCollector toFunnel() {
        return new FunnelCollector();
    }
}
