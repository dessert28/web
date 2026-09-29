package com.zjsru.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageRendersFormAndMessages() throws Exception {
        mockMvc.perform(get("/toLogin").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("用户名或密码错误")))
                .andExpect(content().string(containsString("action=\"/login\"")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("name=\"password\"")))
                .andExpect(content().string(containsString("required")));

        mockMvc.perform(get("/toLogin").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("已安全退出")));
    }

    @Test
    void pagesUseSharedStylesAndMainHasLogoutForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/app.css")));

        mockMvc.perform(get("/toLogin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/app.css")));

        mockMvc.perform(get("/main").with(user("admin").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("method=\"post\"")));
    }
}
