package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult(boolean success, String message, String username, String email, String avatarUrl) {
    public static ApiResult ok(String message, UserProfile profile) {
        return new ApiResult(true, message, profile == null ? null : profile.username(),
                profile == null ? null : profile.email(), profile == null ? null : profile.avatarUrl());
    }

    public static ApiResult fail(String message) {
        return new ApiResult(false, message, null, null, null);
    }
}
