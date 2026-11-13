package com.zjsru.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 实验二：基于 Spring Boot 集成 MyBatis 实现患者挂号
 * <p>
 * 本工程由 jsj2434 复制而来，独立演进：包名 com.zjsru.hospital，端口 8082，
 * 与 jsj2434（8080）共用一个 mybatis 库但互不依赖。
 */
@SpringBootApplication
public class HospitalRegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalRegistrationApplication.class, args);
    }
}
