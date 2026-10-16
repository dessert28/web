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

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
        mockMvc.perform(get("/tologin")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    void validLoginForwardsToMainWithoutCreatingSession() throws Exception {
        var result = mockMvc.perform(post("/login").param("username", "admin").param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("forward:/toMain"))
                .andExpect(forwardedUrl("/toMain"))
                .andReturn();
        assertNull(result.getRequest().getSession(false));
    }

    @Test
    void invalidLoginRendersLoginPage() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin").param("password", "wrong"))
                .andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(post("/login"))
                .andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
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
    void oldEndpointsAreRemoved() throws Exception {
        for (String path : new String[]{"/toLogin", "/main", "/test/alice/secret"}) {
            mockMvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }

    @Test
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
