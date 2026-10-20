package com.zjsru.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** 沿用原测试类名；现用于验证课程登录流程，没有 Spring Security 认证链。 */
// 加载应用上下文，自动配置 MockMvc，在不启动真实端口的情况下模拟 HTTP 请求。
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    // 验证欢迎页和登录页的状态码及视图名。
    void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
        mockMvc.perform(get("/tologin")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    // MockMvc 验证转发目标，不自动执行容器的二次转发；并检查没有创建会话。
    void validLoginForwardsToMainWithoutCreatingSession() throws Exception {
        var result = mockMvc.perform(post("/login").param("username", "admin").param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("forward:/toMain"))
                .andExpect(forwardedUrl("/toMain"))
                .andReturn();
        assertNull(result.getRequest().getSession(false));
    }

    @Test
    // 错误密码及缺失参数都应回到登录页。
    void invalidLoginRendersLoginPage() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin").param("password", "wrong"))
                .andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(post("/login"))
                .andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    // 模型属性仅用于当前视图；session 属性独立存在，模板实际读取 session.username。
    void toMainCarriesUsernameInModelButTemplateReadsSession() throws Exception {
        mockMvc.perform(get("/toMain").param("username", "alice"))
                .andExpect(status().isOk()).andExpect(view().name("main"))
                .andExpect(model().attribute("username", "alice"))
                .andExpect(content().string(not(containsString("alice"))));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "session-user");
        mockMvc.perform(get("/toMain").session(session).param("username", "alice"))
                .andExpect(model().attribute("username", "alice"))
                .andExpect(content().string(containsString("session-user")));
    }

    @Test
    // 旧 URL 应保持 404，不能被全局异常处理改成 HTTP 200。
    void oldEndpointsAreRemoved() throws Exception {
        for (String path : new String[]{"/toLogin", "/main", "/test/alice/secret"}) {
            mockMvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }

    @Test
    // 验证文本与页面两种响应，并明确当前退出演示保留原会话。
    void logoutEndpointsMatchDemoAndPreserveSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "alice");
        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().isOk()).andExpect(content().string("退出成功"));
        mockMvc.perform(get("/logout2").session(session))
                .andExpect(status().isOk()).andExpect(view().name("login"))
                .andExpect(model().attribute("message", "退出成功"))
                .andExpect(content().string(containsString("退出成功")));
        assertFalse(session.isInvalid());
        assertEquals("alice", session.getAttribute("username"));
    }
}
