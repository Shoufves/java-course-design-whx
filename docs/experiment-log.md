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
