package com.zjsru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 应用入口：启用自动配置，从 com.zjsru 包开始扫描控制器、服务及异常处理器。 */
@SpringBootApplication
public class Jsj2434Application {

    public static void main(String[] args) {
        // 创建 Spring 容器并启动内嵌 Web 服务。
        SpringApplication.run(Jsj2434Application.class, args);
    }

}
