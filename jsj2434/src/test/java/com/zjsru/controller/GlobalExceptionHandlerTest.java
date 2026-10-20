package com.zjsru.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zjsru.domain.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 使用真实 Spring Boot 配置测试 JSON 序列化、中文编码及异常分派。 */
// MockMvc 模拟 HTTP；@Import 只在本测试上下文中导入触发异常的控制器。
@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.ExceptionTestController.class)
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    // JVM 生成的空指针详情可能变化，因此只匹配提示前缀。
    void nullPointerReturnsSpecificResponseBody() throws Exception {
        mockMvc.perform(get("/test-exceptions/null-pointer"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value(startsWith("空指针异常：")))
                // hasJsonPath 确认字段存在，nullValue 再确认其值确实为 null。
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    // HTTP 200 与 JSON 业务码 500 分别断言；整数除零由算术处理方法接收。
    void arithmeticReturnsSpecificResponseBody() throws Exception {
        mockMvc.perform(get("/test-exceptions/arithmetic"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("算术异常：/ by zero"))
                // hasJsonPath 确认字段存在，nullValue 再确认其值确实为 null。
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    // 普通 Exception 走兜底方法，保留测试异常的完整消息。
    void otherExceptionReturnsFallbackResponseBody() throws Exception {
        mockMvc.perform(get("/test-exceptions/other"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("其他异常：测试其他异常"))
                // hasJsonPath 确认字段存在，nullValue 再确认其值确实为 null。
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    // 验证成功工厂方法的业务码、提示和数据也能正确序列化。
    void successReturnsFormattedData() throws Exception {
        mockMvc.perform(get("/test-exceptions/success"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"))
                .andExpect(jsonPath("$.data").value("测试数据"));
    }

    @Test
    // 验证正式控制器中的浏览器演示地址也经过全局异常处理。
    void publicDemoReturnsArithmeticJson() throws Exception {
        mockMvc.perform(get("/testException"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("算术异常：/ by zero"))
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // @TestComponent 排除自动组件扫描；仅由本测试的 @Import 显式导入。
    // 这些接口位于测试源码，正式应用中不会提供。
    @TestComponent
    @RestController
    static class ExceptionTestController {
        // 返回正常对象，演示 Result.success 的 JSON 结构。
        @GetMapping("/test-exceptions/success")
        public Result<String> success() {
            return Result.success("测试数据");
        }

        // 故意解引用 null，验证专用空指针处理方法。
        @GetMapping("/test-exceptions/null-pointer")
        public String nullPointer() {
            String value = null;
            return Integer.toString(value.length());
        }

        // 故意使用运行期为零的整数除数，触发 ArithmeticException。
        @GetMapping("/test-exceptions/arithmetic")
        public String arithmetic() {
            int divisor = 0;
            return Integer.toString(10 / divisor);
        }

        // 抛出普通异常，验证 Exception 兜底分支。
        @GetMapping("/test-exceptions/other")
        public String other() throws Exception {
            throw new Exception("测试其他异常");
        }
    }
}
