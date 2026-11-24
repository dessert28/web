package com.zjsru.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 错误页入口。
 *
 * <p>{@code /forbidden} 由 {@code WebAuthConfig} 的 AccessDeniedHandler 转发过来：
 * 已登录但角色不足时，Security 先设置 403 状态，再把请求 forward 到本页，
 * 所以用户看到的是页面而不是空白响应。</p>
 */
@Controller
public class ErrorPageController {

    @GetMapping("/forbidden")
    public String forbidden() {
        return "error/403";
    }
}
