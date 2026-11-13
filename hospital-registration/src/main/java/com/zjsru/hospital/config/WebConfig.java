package com.zjsru.hospital.config;

import com.zjsru.hospital.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册登录拦截器。
 * <p>
 * 只需要登录的路径显式登记；首页 /home、/search、/toLogin、/login 与静态资源不拦。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns(
                        "/department/**",
                        "/doctorDetail/**",
                        "/appointment/**",
                        "/myAppointments",
                        "/doctor/**"
                );
    }
}
