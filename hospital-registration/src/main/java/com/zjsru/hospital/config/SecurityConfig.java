package com.zjsru.hospital.config;

import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.UserCredential;
import com.zjsru.hospital.security.DatabaseUserDetails;
import com.zjsru.hospital.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置：认证走数据库，授权走过滤器链。
 *
 * <p>改造前的实现是「{@code AuthController} 自己查库比对 + {@code LoginInterceptor} 判断
 * session」，现在把两件事分开：</p>
 * <ul>
 *   <li><b>认证</b>：{@link AuthenticationManager} → 自定义 {@link AuthenticationProvider}
 *       → {@link AuthService}（患者查 {@code tb_users}，医生查 {@code doctor_info}）；</li>
 *   <li><b>授权</b>：{@link SecurityFilterChain} 按 URL 判定，{@code /doctor/**} 需要
 *       {@code ROLE_DOCTOR}，其余业务页需要登录；</li>
 *   <li><b>会话</b>：登录成功后把 {@code SecurityContext} 写进 HttpSession，
 *       同时保留原来的 {@code LOGIN_USER} 属性，页面与测试继续可用。</li>
 * </ul>
 *
 * <p>为什么不用 {@code UserDetailsService} + {@code DaoAuthenticationProvider}：
 * 本项目登录有「患者 / 医生」两套表（{@code tb_users} / {@code doctor_info}），
 * 靠表单的 {@code loginType} 选择查询目标。{@code UserDetailsService} 只接收
 * 用户名、不接收 loginType，无法表达这个语义；因此用自定义 provider，
 * 从 {@code Authentication#getDetails()} 里取 loginType 再委托 {@code AuthService}。</p>
 *
 * <p>密码仍是明文比对（课程演示口径）：{@code tb_users.password} 与
 * {@code doctor_info.password} 由 {@code jsj2434} 与项目共用，改哈希会直接破坏那边的登录。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** 登录页（GET），失败时仍由 {@code AuthController} 渲染这个视图。 */
    private static final String LOGIN_PAGE = "/toLogin";

    /**
     * 认证管理器：只挂本项目唯一的数据库 provider。
     * 不注册 {@code UserDetailsService}，避免 Boot 自动生成默认内存用户。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthService authService) {
        return new ProviderManager(databaseAuthenticationProvider(authService));
    }

    /**
     * 数据库认证：把表单提交的用户名/密码/loginType 交给 {@link AuthService} 查库比对。
     * 成功后 principal 直接是 {@link LoginUser}，并映射成 {@code ROLE_PATIENT} / {@code ROLE_DOCTOR}。
     */
    @Bean
    public AuthenticationProvider databaseAuthenticationProvider(AuthService authService) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String loginType = authentication.getDetails() instanceof String value && !value.isBlank()
                        ? value : "patient";

                UserCredential credential = new UserCredential();
                credential.setUsername(authentication.getPrincipal() == null
                        ? null : authentication.getPrincipal().toString());
                credential.setPassword(authentication.getCredentials() == null
                        ? null : authentication.getCredentials().toString());
                credential.setLoginType(loginType);

                LoginUser loginUser = authService.login(credential);
                if (loginUser == null) {
                    throw new BadCredentialsException("用户名或密码错误");
                }

                // 数据库身份 → Spring Security 的 UserDetails（角色映射在 DatabaseUserDetails 里）
                DatabaseUserDetails userDetails = new DatabaseUserDetails(loginUser);
                return UsernamePasswordAuthenticationToken.authenticated(
                        userDetails.getLoginUser(), null, userDetails.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    /**
     * 过滤器链：
     * <ul>
     *   <li>关 CSRF：登录表单与页面没有 token，沿用原行为；</li>
     *   <li>关 formLogin：登录 POST 仍由 {@code AuthController} 处理，失败要渲染 200 的登录页，
     *       Security 默认的「失败 302 到 /toLogin?error」不符合现有页面契约；</li>
     *   <li>未登录 → 302 {@code /toLogin?redirect=原地址}（与改造前的 LoginInterceptor 一致）；</li>
     *   <li>已登录但角色不足 → 403 并转发 {@code /forbidden} 页面。</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .authorizeHttpRequests(auth -> auth
                        // 公开页面与静态资源：首页、检索、登录页、错误页
                        .requestMatchers(
                                "/", "/home", "/search", "/toLogin", "/login", "/logout",
                                "/forbidden", "/error", "/favicon.ico",
                                "/css/**", "/vendor/**", "/webjars/**"
                        ).permitAll()
                        // 医生端只允许医生
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")
                        // 其余业务页必须登录
                        .requestMatchers(
                                "/department/**", "/doctorDetail/**",
                                "/appointment/**", "/myAppointments"
                        ).authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::redirectToLogin)
                        .accessDeniedHandler((request, response, denied) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            request.getRequestDispatcher("/forbidden").forward(request, response);
                        }));
        return http.build();
    }

    /**
     * 未登录：302 到 {@code /toLogin?redirect=<原始 URL>}，保留登录后回跳能力。
     * 编码方式与改造前的 {@code LoginInterceptor} 相同。
     */
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationException exception) throws IOException {
        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }
        String encoded = URLEncoder.encode(target, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + LOGIN_PAGE + "?redirect=" + encoded);
    }
}
