package com.zjsru.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.ExceptionTestController.class)
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void nullPointerReturnsSpecificResponseBody() throws Exception {
        mockMvc.perform(get("/test-exceptions/null-pointer"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("空指针异常：对象为空"));
    }

    @Test
    void arithmeticReturnsSpecificResponseBody() throws Exception {
        mockMvc.perform(get("/test-exceptions/arithmetic"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("算术异常：计算出错"));
    }

    @Test
    void otherExceptionReturnsFallbackResponseBody() throws Exception {
        mockMvc.perform(get("/test-exceptions/other"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("其他异常：系统处理失败"));
    }

    // 仅用于测试，排除在应用组件扫描之外。
    @TestComponent
    @RestController
    static class ExceptionTestController {
        @GetMapping("/test-exceptions/null-pointer")
        public String nullPointer() {
            String value = null;
            return Integer.toString(value.length());
        }

        @GetMapping("/test-exceptions/arithmetic")
        public String arithmetic() {
            int divisor = 0;
            return Integer.toString(10 / divisor);
        }

        @GetMapping("/test-exceptions/other")
        public String other() throws Exception {
            throw new Exception("测试其他异常");
        }
    }
}
