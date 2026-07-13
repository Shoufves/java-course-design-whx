-- ============================================================
-- 数据库名称: course_design
-- 说明: Java课程设计实训 - 用户行为漏斗分析
-- 选题: D - 用户行为漏斗分析
-- 创建时间: 2026-07-13
-- ============================================================

-- 先创建数据库（如果不存在）
-- CREATE DATABASE IF NOT EXISTS course_design
--   DEFAULT CHARACTER SET utf8mb4
--   DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换数据库（如已连接 course_design 可省略）
-- USE course_design;

-- ============================================================
-- 0. 先禁用外键检查，再按依赖顺序删除（先删子表，再删父表）
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `user_log`;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 用户表 (user)
-- 说明: 存储系统用户基本信息
-- ============================================================
CREATE TABLE `user` (
    `id`            BIGINT          NOT NULL    AUTO_INCREMENT    COMMENT '用户唯一ID，主键',
    `username`      VARCHAR(50)     NOT NULL                       COMMENT '用户名，唯一',
    `created_at`    DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 用户行为日志表 (user_log)
-- 说明: 存储用户所有操作行为日志，用于漏斗分析
-- ============================================================
DROP TABLE IF EXISTS `user_log`;
CREATE TABLE `user_log` (
    `id`                BIGINT          NOT NULL    AUTO_INCREMENT    COMMENT '日志唯一ID，主键',
    `user_id`           BIGINT          NOT NULL                       COMMENT '用户ID，外键关联user.id',
    `event_type`        VARCHAR(20)     NOT NULL                       COMMENT '事件类型: view/cart/order/pay',
    `event_time`        DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '事件发生时间',
    `channel`           VARCHAR(20)     DEFAULT NULL                   COMMENT '访问渠道: app/web/miniprogram',
    `device`            VARCHAR(50)     DEFAULT NULL                   COMMENT '设备型号',
    `product_category`  VARCHAR(50)     DEFAULT NULL                   COMMENT '商品类别',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_event_time` (`event_time`),
    CONSTRAINT `fk_user_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为日志表';
