package com.zjsru;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** 加载真实 Spring Boot 上下文，检查应用配置与 Bean 装配是否可用。 */
@SpringBootTest
class Jsj2434ApplicationTests {

    // 无需额外断言；若上下文启动失败，此测试直接失败。
    @Test
    void contextLoads() {
    }

}
