package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 接口统一响应对象：success 表示成败，message 是提示语，username 只在需要时返回。
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult(boolean success, String message, String username) {

    public static ApiResult ok(String message, String username) {
        return new ApiResult(true, message, username);
    }

    public static ApiResult fail(String message) {
        return new ApiResult(false, message, null);
    }
}
