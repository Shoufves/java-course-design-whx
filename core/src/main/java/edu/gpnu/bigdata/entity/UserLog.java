package edu.gpnu.bigdata.entity;

import java.sql.Timestamp;

/**
 * 用户行为日志实体类（使用record定义不可变DTO）
 * record是Java 17+的特性，自动生成构造器、getter、equals、hashCode
 */
public record UserLog(
        Long id,              // 日志ID
        Long userId,          // 用户ID
        String eventType,     // 事件类型: view/cart/order/pay
        Timestamp eventTime,  // 事件时间
        String channel,       // 渠道: app/web/miniprogram
        String device,        // 设备
        String productCategory // 商品类别
) {}
