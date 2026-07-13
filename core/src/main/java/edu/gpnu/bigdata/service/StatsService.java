package edu.gpnu.bigdata.service;

import edu.gpnu.bigdata.collector.FunnelCollector;
import edu.gpnu.bigdata.dao.UserLogDao;
import edu.gpnu.bigdata.entity.UserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 统计分析服务
 * 使用Stream API对用户行为日志进行聚合统计
 */
public class StatsService {
    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);
    private final UserLogDao dao;

    /**
     * 构造函数注入（用于测试，方便Mockito注入Mock对象）
     */
    public StatsService(UserLogDao dao) {
        this.dao = dao;
    }

    /**
     * 默认构造函数（生产环境使用，使用真实DAO）
     */
    public StatsService() {
        this.dao = new UserLogDao();
    }

    // ========== 统计方法 ==========

    /**
     * 统计1：按事件类型分组统计
     */
    public Map<String, Long> countByEventType() {
        List<UserLog> logs = dao.findAll();
        Map<String, Long> result = logs.stream()
                .collect(Collectors.groupingBy(
                        UserLog::eventType,
                        Collectors.counting()
                ));
        logger.info("按事件类型统计完成: {}", result);
        return result;
    }

    /**
     * 统计2：按渠道分组统计
     */
    public Map<String, Long> countByChannel() {
        List<UserLog> logs = dao.findAll();
        Map<String, Long> result = logs.stream()
                .collect(Collectors.groupingBy(
                        UserLog::channel,
                        Collectors.counting()
                ));
        logger.info("按渠道统计完成: {}", result);
        return result;
    }

    /**
     * 统计3：按天统计PV和UV
     */
    public Map<LocalDate, Map<String, Long>> countDailyPVUV() {
        List<UserLog> logs = dao.findAll();

        Map<LocalDate, Long> dailyPV = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.eventTime().toLocalDateTime().toLocalDate(),
                        Collectors.counting()
                ));

        Map<LocalDate, Integer> dailyUV = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.eventTime().toLocalDateTime().toLocalDate(),
                        Collectors.mapping(
                                UserLog::userId,
                                Collectors.collectingAndThen(
                                        Collectors.toSet(),
                                        Set::size
                                )
                        )
                ));

        Map<LocalDate, Map<String, Long>> result = new LinkedHashMap<>();
        Set<LocalDate> allDates = new HashSet<>(dailyPV.keySet());
        allDates.addAll(dailyUV.keySet());

        for (LocalDate date : allDates.stream().sorted().toList()) {
            Map<String, Long> stats = new LinkedHashMap<>();
            stats.put("PV", dailyPV.getOrDefault(date, 0L));
            stats.put("UV", dailyUV.getOrDefault(date, 0).longValue());
            result.put(date, stats);
        }

        logger.info("按天统计完成，共 {} 天数据", result.size());
        return result;
    }

    /**
     * 统计4：漏斗转化率（使用内置Collector）
     */
    public Map<String, Object> calculateFunnel() {
        List<UserLog> logs = dao.findAll();

        Map<String, Set<Long>> eventUsers = logs.stream()
                .collect(Collectors.groupingBy(
                        UserLog::eventType,
                        Collectors.mapping(
                                UserLog::userId,
                                Collectors.toSet()
                        )
                ));

        String[] funnelSteps = {"view", "cart", "order", "pay"};
        Map<String, Long> stepCounts = new LinkedHashMap<>();
        for (String step : funnelSteps) {
            Set<Long> users = eventUsers.getOrDefault(step, Collections.emptySet());
            stepCounts.put(step, (long) users.size());
        }

        List<Map<String, Object>> conversionRates = new ArrayList<>();
        Long prevCount = null;
        String prevStep = null;

        for (String step : funnelSteps) {
            Long currentCount = stepCounts.get(step);
            Map<String, Object> rateInfo = new LinkedHashMap<>();
            rateInfo.put("step", step);
            rateInfo.put("userCount", currentCount);

            if (prevCount != null && prevCount > 0) {
                double rate = (double) currentCount / prevCount * 100;
                rateInfo.put("conversionRate", String.format("%.2f%%", rate));
                rateInfo.put("from", prevStep);
            } else {
                rateInfo.put("conversionRate", "100.00%");
                rateInfo.put("from", "起始");
            }

            conversionRates.add(rateInfo);
            prevCount = currentCount;
            prevStep = step;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stepCounts", stepCounts);
        result.put("conversionRates", conversionRates);

        logger.info("漏斗统计完成: {}", stepCounts);
        return result;
    }

    /**
     * 统计5：商品类别Top N
     */
    public List<Map.Entry<String, Long>> topCategories(int topN) {
        List<UserLog> logs = dao.findAll();

        List<Map.Entry<String, Long>> result = logs.stream()
                .filter(log -> log.productCategory() != null && !log.productCategory().isEmpty())
                .collect(Collectors.groupingBy(
                        UserLog::productCategory,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .toList();

        logger.info("Top{}商品类别: {}", topN, result);
        return result;
    }

    // ========== 以下为3.1新增方法 ==========

    /**
     * 统计6：使用自定义FunnelCollector一次性完成漏斗统计
     */
    public Map<String, Long> countFunnelWithCustomCollector() {
        List<UserLog> logs = dao.findAll();
        Map<String, Long> result = logs.stream()
                .collect(FunnelCollector.toFunnel());
        logger.info("自定义收集器漏斗统计完成: {}", result);
        return result;
    }

    /**
     * 统计7：使用并行流按事件类型统计（大数据量自动切换）
     */
    public Map<String, Long> countByEventTypeParallel() {
        List<UserLog> logs = dao.findAll();
        boolean useParallel = logs.size() > 50_000;
        Map<String, Long> result;
        if (useParallel) {
            logger.info("数据量{}，使用并行流", logs.size());
            result = logs.parallelStream()
                    .collect(Collectors.groupingBy(
                            UserLog::eventType,
                            Collectors.counting()
                    ));
        } else {
            logger.info("数据量{}，使用顺序流", logs.size());
            result = logs.stream()
                    .collect(Collectors.groupingBy(
                            UserLog::eventType,
                            Collectors.counting()
                    ));
        }
        logger.info("并行流按事件类型统计完成: {}", result);
        return result;
    }

    /**
     * 统计8：并行流 + 线程安全容器统计各事件独立用户数
     */
    public Map<String, Set<Long>> countEventUsersParallel() {
        List<UserLog> logs = dao.findAll();
        Map<String, Set<Long>> eventUsers = logs.parallelStream()
                .collect(
                        ConcurrentHashMap::new,
                        (map, log) -> map.computeIfAbsent(
                                log.eventType(),
                                k -> ConcurrentHashMap.newKeySet()
                        ).add(log.userId()),
                        (map1, map2) -> {
                            map2.forEach((key, set) -> {
                                map1.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                                        .addAll(set);
                            });
                        }
                );
        logger.info("并行流统计各事件用户数完成");
        return eventUsers;
    }
}
