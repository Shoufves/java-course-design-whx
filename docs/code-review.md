# 代码审查报告

**审查日期**：2026-07-13
**审查人**：吴浩轩
**被审查项目**：java-course-design-whx

## 一、审查范围
- [x] core模块（entity/dao/service/collector/dto/util）
- [x] web模块（ApiServer）
- [x] benchmark模块（JMH测试）
- [x] 配置文件（pom.xml、application.properties、logback.xml）
- [x] 测试代码

## 二、审查结果

### 代码规范
- [x] 命名规范符合要求（驼峰命名，类名大写开头，方法/字段小写开头）
- [x] 代码格式统一（已通过 IDE 自动格式化）
- [x] 注释清晰完整（类和公共方法都有 JavaDoc 注释）

### 设计质量
- [x] 分层结构清晰（entity/dao/service/collector/dto/util）
- [x] 类职责单一（每个类只负责一个业务领域）
- [x] 模块间低耦合（core 独立，web 依赖 core，benchmark 依赖 core）

### 异常处理
- [x] 资源使用 try-with-resources（ConfigLoader、UserLogDao、CacheService）
- [x] 异常有完整日志（SLF4J + Logback，包含异常堆栈）
- [x] 自定义异常使用 RuntimeException 包装

### 性能
- [x] DataGenerator 使用 BATCH_SIZE=1000 + rewriteBatchedStatements
- [x] CacheService 使用 JedisPool 连接池
- [x] ReportGenerator 使用 StringBuilder 拼接字符串
- [x] StatsService 使用 Stream API 延迟求值

### 线程安全
- [x] 并行流统计使用 ConcurrentHashMap 线程安全容器
- [x] FunnelCollector 不加 CONCURRENT 特性（非线程安全 HashMap）
- [x] CacheService 的 JedisPool 是线程安全的

### 测试覆盖
- [x] StatsServiceTest（9个测试，Mockito 模拟 DAO 层）
- [x] FunnelCollectorTest（4个测试，覆盖基础/空数据/顺序/合并）
- [x] CacheServiceTest（3个测试，覆盖写/读/删）
- [x] UserLogDaoTest（1个测试，集成测试需 MySQL）
- [x] ConfigLoaderTest（4个测试，覆盖存在/不存在/默认值/Redis配置）
- [x] StatsDTOTest（2个测试，覆盖空/非空）
- [x] ApiResponseTest（3个测试，覆盖成功/无数据/错误）
- [x] 总测试数：26个，全部通过

## 三、发现的问题

| 序号 | 问题描述 | 严重程度 | 状态 |
|------|----------|----------|------|
| 1 | JMH 基准测试方法 testCustomCollector 与 testSequentialStream 都会查询数据库，JMH 测量结果包含 JDBC 查询时间而非纯 CPU 计算时间 | 低 | 已分析（见下方说明） |
| 2 | ReportGenerator 使用 new StatsService() 硬编码，不利于测试注入 | 中 | 当前可接受（4.1 已补充集成测试） |
| 3 | CacheWarmupService.warmupAsync() 使用 CompletableFuture.runAsync，异常处理使用 exceptionally() 会吞没异常 | 低 | 当前可接受（30秒超时保护） |

### 问题 1 说明
JMH 测试的是"完整查询+统计"的端到端吞吐量，而非纯 Stream CPU 性能。这是设计上的选择——因为在实际中数据库 I/O 是瓶颈，JMH 结果反映的是真实场景。若要测量纯 Stream 性能，需在 @Setup 中预加载数据到内存。

## 四、改进建议
1. ReportGenerator 可改为构造函数注入 StatsService，便于单元测试
2. CacheWarmupService 可提取接口，方便异步逻辑的测试
3. DataGenerator 可拆分为"生成"和"插入"两个阶段，便于测试生成的逻辑
4. 可以在 CI 中配置 JaCoCo 覆盖率检查，低于 60% 构建失败
