package com.zjsru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Spring Security 链路测试（走真实过滤器链，不设 addFilters=false）：
 * 公开页放行、未登录拦截、登录成功/失败、注销。
 * 依赖数据库账号 admin / 1234（sys_user 表，1117 样本种子），
 * 不连业务库之外的东西。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    /** 由 DataBaseUserDeatilsService 提供，验证 userdata 层。 */
    @Autowired
    private UserDetailsService userDetailsService;

    // 公开页：登录页与首页未登录可访问
    @Test
    public void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
        // 旧地址保留兼容
        mockMvc.perform(get("/toLogin"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    // 受保护路由：未登录访问应重定向到登录页
    @Test
    public void protectedRouteRedirectsToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/departmentList"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // 正确账号密码：POST /login 登录成功，重定向到 /selectAllUsers
    @Test
    public void loginWithValidCredentialsSucceeds() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/selectAllUsers"));
    }

    // 错误密码：重定向回登录页并带 error 参数
    @Test
    public void loginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // sys_user 里的 doctor 账号同样能登录，并按角色进医生列表（不是管理员用户列表）
    @Test
    public void doctorAccountFromDatabaseCanLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "doctor")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctorList"));
    }

    // 旧的明文口令 123 必须失效：库里的密码是 BCrypt 的 1234
    @Test
    public void oldPlainPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // 不存在的账号同样按「用户名或密码错误」处理，不泄露账号是否存在
    @Test
    public void unknownAccountIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "nobody")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // 患者：登录落到科室列表；用户管理与医生列表都进不去
    @Test
    public void patientLandsOnDepartmentListAndIsRestricted() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", "patient")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departmentList"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get("/selectAllUsers").session(session))
                .andExpect(status().isForbidden());
        // 医生写操作对患者关闭（GET 也会被授权拦截，不会真正执行插入）
        mockMvc.perform(get("/addDoctor").session(session))
                .andExpect(status().isForbidden());
        // 浏览类对患者开放，科室页的「医生列表」导航不会点出 403
        mockMvc.perform(get("/doctorList").session(session))
                .andExpect(status().isOk());
    }

    // 医生：能进医生列表，用户管理 403
    @Test
    public void doctorIsRestrictedFromUserManagement() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", "doctor")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get("/doctorList").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/selectAllUsers").session(session))
                .andExpect(status().isForbidden());
    }

    // 管理员：用户列表、医生列表、科室列表三块都能进
    @Test
    public void adminCanAccessAllAreas() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get("/selectAllUsers").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/doctorList").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/departmentList").session(session))
                .andExpect(status().isOk());
    }

    // 登录成功后，带着会话访问受保护路由应放行（200）
    @Test
    public void authenticatedSessionCanAccessProtectedRoute() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/departmentList").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("departmentList"));
    }

    // 注销：POST /logout 后会话失效，再访问受保护路由应被重定向
    @Test
    public void logoutInvalidatesSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        mockMvc.perform(get("/departmentList").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // 登录页把 ?error / ?logout 翻译成可见提示，而不是只改 URL
    @Test
    public void loginPageShowsErrorAndLogoutHints() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("用户名或密码错误")));

        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("已安全退出")));
    }

    // userdata 层：sys_user_role 关联出的角色要变成 ROLE_* 权限
    @Test
    public void databaseUserDetailsCarriesRoles() {
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        assertNotNull(admin);
        assertTrue(admin.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));

        UserDetails doctor = userDetailsService.loadUserByUsername("doctor");
        assertTrue(doctor.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority())));

        UserDetails patient = userDetailsService.loadUserByUsername("patient");
        assertTrue(patient.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority())));
    }

    // 不存在的账号必须抛 UsernameNotFoundException，而不是返回空对象
    @Test
    public void unknownUserThrowsUsernameNotFound() {
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nobody"));
    }
}
