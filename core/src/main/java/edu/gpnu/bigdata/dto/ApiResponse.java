package edu.gpnu.bigdata.dto;

/**
 * 统一API响应格式
 * 所有REST API端点返回此格式的JSON
 */
public record ApiResponse<T>(
        int code,        // 状态码：200成功，400失败
        String message,  // 响应消息
        T data           // 响应数据（泛型）
) {
    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
