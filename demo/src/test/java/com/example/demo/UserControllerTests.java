package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {
    @Autowired MockMvc mockMvc;

    @Test
    void registrationLoginHomeAndLogoutFlowWorks() throws Exception {
        mockMvc.perform(post("/register").param("username", "pageUser01").param("email", "page@example.com")
                        .param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
        MvcResult login = mockMvc.perform(post("/login").param("username", "pageUser01").param("password", "secret1"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home")).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/home").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("pageUser01")))
                .andExpect(content().string(containsString("page@example.com")));
        mockMvc.perform(post("/logout").session(session)).andExpect(redirectedUrl("/login?logout"));
        mockMvc.perform(get("/home").session(session)).andExpect(redirectedUrl("/login"));
    }

    @Test
    void registrationRejectsDuplicateEmailAndMismatchedPasswords() throws Exception {
        register("pageUser02", "second@example.com", "secret1", "secret1").andExpect(status().is3xxRedirection());
        register("pageUser03", "second@example.com", "secret1", "secret1").andExpect(status().isOk()).andExpect(content().string(containsString("邮箱已存在")));
        register("pageUser04", "fourth@example.com", "secret1", "secret2").andExpect(status().isOk()).andExpect(content().string(containsString("两次密码不一致")));
    }

    @Test
    void registrationAcceptsAvatarUpload() throws Exception {
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1};
        mockMvc.perform(multipart("/register").file("avatar", png).param("username", "pageUser05")
                        .param("email", "avatar@example.com").param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
    }

    private org.springframework.test.web.servlet.ResultActions register(String username, String email, String password, String confirm) throws Exception {
        return mockMvc.perform(post("/register").param("username", username).param("email", email).param("password", password).param("confirmPassword", confirm));
    }
}
