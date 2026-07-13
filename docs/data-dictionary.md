# 数据字典 - 用户行为漏斗分析

## 表级信息

| 表名 | 中文名 | 说明 | 主键 | 外键 |
|------|--------|------|------|------|
| user | 用户表 | 存储系统用户基本信息 | id | 无 |
| user_log | 用户行为日志表 | 存储用户所有操作行为日志，用于漏斗分析 | id | user_id → user.id |

## 1. 用户表 (user)

| 字段名 | 数据类型 | 允许空 | 默认值 | 约束 | 说明 |
|--------|----------|--------|--------|------|------|
| id | BIGINT | 否 | 自增 | PRIMARY KEY | 用户唯一ID |
| username | VARCHAR(50) | 否 | 无 | UNIQUE | 用户名 |
| created_at | DATETIME | 否 | CURRENT_TIMESTAMP | 无 | 注册时间 |

## 2. 用户行为日志表 (user_log)

| 字段名 | 数据类型 | 允许空 | 默认值 | 约束 | 说明 |
|--------|----------|--------|--------|------|------|
| id | BIGINT | 否 | 自增 | PRIMARY KEY | 日志唯一ID |
| user_id | BIGINT | 否 | 无 | FOREIGN KEY (user.id) | 用户ID |
| event_type | VARCHAR(20) | 否 | 无 | INDEX | 事件类型：view/cart/order/pay |
| event_time | DATETIME | 否 | CURRENT_TIMESTAMP | INDEX | 事件发生时间 |
| channel | VARCHAR(20) | 是 | NULL | 无 | 访问渠道：app/web/miniprogram |
| device | VARCHAR(50) | 是 | NULL | 无 | 设备型号 |
| product_category | VARCHAR(50) | 是 | NULL | 无 | 商品类别 |
