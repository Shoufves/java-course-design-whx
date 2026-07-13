# ============================================================
# Dockerfile - Java课程设计实训 - Javalin REST API
# 基于 JDK 21 的多阶段构建
# ============================================================

# ---- 构建阶段 ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 先复制 POM 文件，利用 Docker 缓存加速依赖下载
COPY pom.xml ./
COPY core/pom.xml ./core/pom.xml
COPY benchmark/pom.xml ./benchmark/pom.xml
COPY web/pom.xml ./web/pom.xml

# 下载依赖（不编译源码）
RUN mvn dependency:go-offline -q

# 复制全部源码
COPY . .

# 构建胖 JAR（跳过测试）
RUN mvn package -pl web -DskipTests -q

# ---- 运行阶段 ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# 从构建阶段复制胖 JAR
COPY --from=builder /app/web/target/web-1.0-SNAPSHOT-shaded.jar app.jar

# 暴露 REST API 端口
EXPOSE 8080

# 启动 ApiServer
ENTRYPOINT ["java", "-jar", "app.jar"]
