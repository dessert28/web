package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiTests {
    @Autowired MockMvc mockMvc;

    @Test
    void apiRegisterLoginMeAndLogoutReturnsProfileFields() throws Exception {
        String user = "apiUser01";
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(user, "api@example.com", "secret1", "secret1")))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.email").value("api@example.com"));
        MvcResult login = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(user, "api@example.com", "secret1", null)))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.username").value(user)).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/api/me").session(session)).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.email").value("api@example.com"));
        mockMvc.perform(post("/api/logout").session(session)).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void apiRejectsInvalidRegistrationAndWrongPassword() throws Exception {
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(body("apiUser02", "bad", "secret1", "secret1")))
                .andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.message").value("邮箱格式不正确"));
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(body("missing", "x@y.com", "wrong1", null)))
                .andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.message").value(containsString("用户名或密码错误")));
    }

    private String body(String username, String email, String password, String confirm) {
        return "{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"" + (confirm == null ? "" : ",\"confirmPassword\":\"" + confirm + "\"") + "}";
    }
}
