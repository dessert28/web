package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrationCreatesAccountAndRejectsDuplicateUsername() throws Exception {
        register("alice01", "secret1", "secret1")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        register("alice01", "secret2", "secret2")
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("用户名已存在")));
    }

    @Test
    void registrationRejectsMismatchedPasswords() throws Exception {
        register("bob01", "secret1", "secret2")
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("两次密码不一致")));
    }

    @Test
    void registrationRejectsShortUsernameAndPassword() throws Exception {
        register("ab", "12345", "12345")
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("用户名长度")));
    }

    @Test
    void successfulLoginCreatesSessionAndHomeShowsUsername() throws Exception {
        register("carol01", "secret1", "secret1").andExpect(status().is3xxRedirection());

        MvcResult login = mockMvc.perform(post("/login")
                        .param("username", "carol01")
                        .param("password", "secret1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/home").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("carol01")));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        register("dave01", "secret1", "secret1").andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/login")
                        .param("username", "dave01")
                        .param("password", "wrong1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void unauthenticatedHomeRedirectsToLoginAndLogoutInvalidatesSession() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        register("erin01", "secret1", "secret1").andExpect(status().is3xxRedirection());
        MvcResult login = mockMvc.perform(post("/login")
                        .param("username", "erin01")
                        .param("password", "secret1"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
        mockMvc.perform(get("/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private org.springframework.test.web.servlet.ResultActions register(
            String username, String password, String confirmPassword) throws Exception {
        return mockMvc.perform(post("/register")
                .param("username", username)
                .param("password", password)
                .param("confirmPassword", confirmPassword));
    }
}
