package com.zjsru.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** 通过实际模板渲染结果验证表单字段、提示和链接，防止页面与控制器路径脱节。 */
// MockMvc 在应用上下文中模拟请求，无需启动浏览器或占用端口。
@SpringBootTest
@AutoConfigureMockMvc
class UserPageTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    // 校验 POST 表单地址、字段名、退出提示及不再使用旧样式。
    void loginPageAndLogoutMessageMatchDemo() throws Exception {
        mockMvc.perform(get("/tologin"))
                .andExpect(status().isOk()).andExpect(view().name("login"))
                .andExpect(content().string(containsString("action=\"/login\" method=\"post\"")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("name=\"password\"")))
                .andExpect(content().string(not(containsString("/css/app.css"))));
        mockMvc.perform(get("/logout2"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("退出成功")));
    }

    @Test
    // 校验欢迎页及主页链接，确认已移除的样式资源仍返回 404。
    void welcomePageAndMainLinksMatchDemo() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("欢迎来到我的网站")))
                .andExpect(content().string(containsString("href=\"/tologin\"")));
        mockMvc.perform(get("/toMain"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("这里是主页")))
                .andExpect(content().string(containsString("href=\"/logout\"")))
                .andExpect(content().string(containsString("href=\"/logout2\"")));
        mockMvc.perform(get("/css/app.css")).andExpect(status().isNotFound());
    }
}
