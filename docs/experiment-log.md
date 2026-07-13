# Java课程设计实训实验记录

**姓名**：吴浩轩
**学号**：2024035043045
**选题**：D-用户行为漏斗分析

---

## 第1天（日期：2026年7月6日）

### 上午：环境搭建与项目初始化

#### 任务完成情况
- [x] JDK 17/21 安装与配置
- [x] Maven 安装与配置
- [x] Git 安装与配置
- [x] IntelliJ IDEA 安装
- [x] Docker Desktop 安装（可选）
- [x] 项目模板创建/克隆
- [x] MySQL/Redis 容器启动
- [x] 外部化配置（application.properties）配置与验证
- [x] ConfigLoader 工具类完成，main()测试通过
- [x] 父 POM 补充 JaCoCo + assertj 依赖

#### 遇到的问题与解决
没有遇到什么问题。

#### 今日小结
完成了 JDK 17、Maven 和 Git 的安装与配置，并安装了 IntelliJ IDEA 作为开发工具。同时搭建了 Docker 环境，启动 MySQL 和 Redis 容器。基于 Maven 多模块模板创建了项目结构，确保各模块之间的依赖关系正确。配置了外部化加载的 application.properties，并添加了 ConfigLoader 的测试 main 方法验证配置加载。

### 下午：数据库设计与日志配置

#### 任务完成情况
- [x] 绘制 E-R 图（Draw.io，导出为 docs/er-diagram.png）
- [x] 编写数据字典（docs/data-dictionary.md）
- [x] 编写建表 SQL（sql/schema.sql）
- [x] 配置 Logback（控制台 + 文件双输出）

#### 遇到的问题与解决
- 问题：首次启动 ApiServer 时，后台异步预热报错 `Table 'course_design.user_log' doesn't exist`。
- 解决：这是因为还没有执行建表 SQL。执行 `sql/schema.sql` 创建表后，重启 ApiServer，报错消失，日志变为“查询完成，共读取 0 条记录”。


#### 今日小结
根据选题D（用户行为漏斗分析）的业务需求，设计了 user 和 user_log 两张核心表，绘制了 E-R 图并编写了数据字典。建表 SQL 包含主键、外键、索引和注释。Logback 日志框架配置为控制台和文件双输出，支持按天滚动。

---

## 第2天（日期：2026年7月7日）

### 上午：数据生成与批量导入

#### 任务完成情况
- [x] DataFaker 依赖已在 core/pom.xml 中确认
- [x] 编写 DataGenerator.java（用户生成 + 行为日志生成）
- [x] HikariCP 连接池通过 DataSourceFactory 自动管理
- [x] JDBC 批量插入（BATCH_SIZE=1000）
- [x] 执行 10 万条数据生成与导入
- [x] 验证数据完整性

#### 遇到的问题与解决
没有遇到什么问题。

#### 今日小结
使用 DataFaker 生成了 10,000 个用户和 100,000 条用户行为日志，通过 JDBC 批量插入 + rewriteBatchedStatements=true 实现高速写入，总耗时仅 3 秒。验证确认：总记录数 100,000、事件类型分布 view(25K) > cart(25K) > order(25K) > pay(25K)、独立用户约 9,999、用户数 10,000。

### 下午：Stream API聚合与Redis缓存

#### 任务完成情况
- [x] 创建 UserLog.java（record不可变DTO）
- [x] 创建 UserLogDao.java（数据访问层）
- [x] 创建 StatsService.java（5种Stream API聚合统计）
- [x] 创建 ReportGenerator.java（文本报表生成器）
- [x] 创建 CacheService.java（Jedis Redis缓存服务）
- [x] 创建 StatsIntegrationTest.java（集成测试）
- [x] 运行集成测试，所有统计结果正确
- [x] Redis 缓存写入/读取验证通过
- [x] report.txt 报表已生成

#### 遇到的问题与解决
- 问题：首次运行 StatsIntegrationTest 时，mvn exec:java 报 `ClassNotFoundException`，找不到 test 目录下的类。
- 解决：exec:java 默认只在 main 源码目录执行，添加参数 `-Dexec.classpathScope=test` 后，test 目录下的类也能被加载运行。

- 问题：漏斗转化率统计结果显示 view→cart 转化率达到 100.23%（超过 100%），不符合业务直觉。
- 解决：分析发现这是因为数据是随机生成的，user_id 在各事件类型中均匀分布，并非真实的漏斗行为序列。这是合成数据的自然特性，真实的用户行为漏斗需要按时间序列计算每个用户的转化路径，后续可使用自定义收集器优化。

#### 今日小结
完成了 Stream API 聚合统计的全部 5 种分析方法：按事件类型、按渠道、按天 PV/UV、漏斗转化率、商品类别 TopN。统计结果通过 Jedis 缓存到 Redis（key: stats:eventType，过期时间 1 小时），并生成了格式化的文本报表 report.txt。集成测试验证了从数据库读取 → Stream 聚合 → Redis 缓存 → 报表导出的完整链路。

---

## 第3天（日期：2026年7月13日）

### 上午：Stream高级特性、JMH基准测试与Mockito单元测试

#### 任务完成情况
- [x] 理解 Collector 接口的五个核心方法
- [x] 创建 FunnelCollector.java 自定义收集器（实现 Collector 接口全部5方法）
- [x] StatsService 追加 countFunnelWithCustomCollector() 方法
- [x] StatsService 追加并行流统计方法（countByEventTypeParallel / countEventUsersParallel）
- [x] 创建 StreamBenchmark.java（JMH基准测试，3个@Benchmark方法）
- [x] 运行 JMH 基准测试
- [x] 创建 StatsServiceTest.java（Mockito 模拟 DAO 层）
- [x] 6个单元测试全部通过

#### 遇到的问题与解决
- 问题：JMH 通过 mvn exec:java 运行时 forked VM 报 ClassNotFoundException: ForkedMain。
- 解决：JMH 分叉模式需要独立的 classpath。改用 mvn package 构建 benchmark/target/benchmarks.jar，再通过 java -jar 直接运行，三个 @Benchmark 全部执行成功。

#### 今日小结
完成了自定义 FunnelCollector 的编写，实现了 Collector 接口的 supplier/accumulator/combiner/finisher/characteristics 五个方法，支持顺序流和并行流两种模式。并行流统计使用了 ConcurrentHashMap 线程安全容器。JMH 基准测试对比了顺序流(8.69 ops/s)、并行流(9.27 ops/s)和自定义收集器(8.30 ops/s)的吞吐量，在10万条数据规模下并行流略优但差异不大。Mockito 单元测试通过模拟 DAO 层实现了6个测试用例，无需真实数据库即可验证统计逻辑。
