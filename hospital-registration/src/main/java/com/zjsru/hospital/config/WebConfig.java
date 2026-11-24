package com.zjsru.hospital.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置。
 *
 * <p>登录拦截与医生端权限已迁移到 Spring Security：
 * 未登录由 {@code SecurityConfig} 的 {@code AuthenticationEntryPoint} 302 到
 * {@code /toLogin?redirect=...}，角色不足由 {@code AccessDeniedHandler} 返回 403。
 * 因此这里不再注册 {@code LoginInterceptor}。</p>
 *
 * <p>{@code LoginInterceptor} 类保留，因为它的 {@code SESSION_USER} 会话键仍被
 * 页面控制器与既有测试引用，不能删。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 授权已由 SecurityFilterChain 承担；拦截器不再参与登录判定与 403 处理。
    }
}
