package com.zjsru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * Spring Security 配置：认证走数据库，授权按 {@code sys_role} 分权。
 *
 * <p>账号与角色来自 1117 样本的三表：{@code sys_user} / {@code sys_role} /
 * {@code sys_user_role}，由 {@link com.zjsru.service.DataBaseUserDeatilsService} 加载。</p>
 *
 * <h3>授权矩阵</h3>
 * <ul>
 *   <li><b>ADMIN</b>：用户管理（用户列表、用户查询）、医生增改、科室与医生浏览；</li>
 *   <li><b>DOCTOR</b>：医生增改、科室与医生浏览；不能进用户管理；</li>
 *   <li><b>PATIENT</b>：科室与医生浏览（含医生列表与条件查询）；不能进用户管理，也不能增改医生；</li>
 *   <li>公共：首页、登录页、静态资源、错误页。</li>
 * </ul>
 * <p>科室页与医生页之间互相有导航链接，所以「浏览类」对三种角色同时开放；
 * 真正的边界放在用户管理与医生写操作上，避免点导航就 403。</p>
 *
 * <h3>登录后跳转</h3>
 * <p>按角色跳转，且跳转目标一定在该角色权限内，避免「登录成功立刻 403」：
 * ADMIN → {@code /selectAllUsers}，DOCTOR → {@code /doctorList}，
 * PATIENT → {@code /departmentList}。</p>
 */
@Configuration
@EnableWebSecurity
public class WebAuthConfig {

    /** 密码编码器：BCrypt。库里的 password 列存的就是 BCrypt 哈希。 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 过滤器链：公开页放行，业务路由按角色授权。 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 课程演示：login.html 表单未带 CSRF token，关闭 CSRF 才能沿用现有页面
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 公开页面与静态资源
                        .requestMatchers(
                                "/", "/toLogin", "/login", "/logout", "/forbidden",
                                "/css/**", "/vendor/**", "/webjars/**",
                                "/favicon.ico", "/error"
                        ).permitAll()

                        // 用户管理：只有管理员
                        .requestMatchers(
                                "/selectAllUsers", "/toMain",
                                "/queryUserById", "/queryUserById/**",
                                "/queryUserByNameAndPassword", "/queryUserByNameAndPassword/**"
                        ).hasRole("ADMIN")

                        // 医生数据维护：管理员、医生
                        .requestMatchers("/addDoctor", "/updateDoctor").hasAnyRole("ADMIN", "DOCTOR")

                        // 浏览类：科室页与医生页互相导航，三种角色都可看
                        .requestMatchers(
                                "/doctorList", "/doctorQuery",
                                "/queryAllDoctors", "/queryDoctorById", "/queryDoctorById/**",
                                "/queryDoctorsByCondition", "/queryDoctorsBySingleCondition",
                                "/departmentList", "/queryDepartmentInfoAndDoctorsByDeptId"
                        ).hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        // 其余业务路由登录即可
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(roleBasedSuccessHandler())  // 按角色跳转
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // 已登录但角色不足：保持 403 状态并转发到有内容的错误页，而不是空响应
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, denied) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            request.getRequestDispatcher("/forbidden").forward(request, response);
                        }));
        return http.build();
    }

    /**
     * 登录成功后的跳转目标由角色决定，优先级 ADMIN → DOCTOR → PATIENT。
     * 没有可识别角色时回首页，避免把人送进 403。
     */
    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            String target = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .map(role -> switch (role) {
                        case "ADMIN" -> "/selectAllUsers";
                        case "DOCTOR" -> "/doctorList";
                        case "PATIENT" -> "/departmentList";
                        default -> null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("/");
            response.sendRedirect(request.getContextPath() + target);
        };
    }
}
