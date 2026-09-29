package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.storage-root:.}")
    private String storageRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(storageRoot).toAbsolutePath().normalize().resolve("uploads").toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
