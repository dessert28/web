package com.zjsru.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get("/toLogin"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void mainRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/main"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/toLogin"));
    }

    @Test
    void validLoginRedirectsToMain() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    @Test
    void invalidLoginRedirectsWithError() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/toLogin?error"));
    }

    @Test
    void authenticatedUserCanOpenMain() throws Exception {
        mockMvc.perform(get("/main").with(user("admin").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("main"));
    }

    @Test
    void logoutEndsSession() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/toLogin?logout"));
    }

    @Test
    void logoutRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin")))
                .andExpect(status().isForbidden());
    }

}


