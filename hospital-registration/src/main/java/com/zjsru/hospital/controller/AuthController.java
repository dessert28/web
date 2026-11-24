package com.zjsru.hospital.controller;

import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.UserCredential;
import com.zjsru.hospital.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录 / 退出。
 *
 * <p>认证已交给 Spring Security：{@code /login} 把凭据交给 {@link AuthenticationManager}，
 * 由 {@code SecurityConfig} 注册的数据库 provider 查 {@code tb_users} / {@code doctor_info} 比对。
 * 认证成功后同时写入两处状态：</p>
 * <ul>
 *   <li>Spring Security 的 {@code SecurityContext}（由过滤器链读，负责授权）；</li>
 *   <li>原来的 {@code LOGIN_USER} session 属性（页面导航与 {@code LoginInterceptor} 仍在用）。</li>
 * </ul>
 * <p>失败仍返回 200 的登录视图加错误提示，保持既有页面契约。</p>
 */
@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/toLogin")
    public ModelAndView toLogin(@RequestParam(value = "redirect", required = false) String redirect,
                                HttpSession session) {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("redirect", redirect);
        mv.addObject("loginUser", session.getAttribute(LoginInterceptor.SESSION_USER));
        return mv;
    }

    @PostMapping("/login")
    public ModelAndView login(@RequestParam("username") String username,
                              @RequestParam("password") String password,
                              @RequestParam(value = "loginType", required = false, defaultValue = "patient")
                              String loginType,
                              @RequestParam(value = "redirect", required = false) String redirect,
                              HttpSession session) {
        LoginUser loginUser;
        try {
            UsernamePasswordAuthenticationToken token =
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password);
            // loginType 通过 details 传给自定义 AuthenticationProvider，决定查哪张表
            token.setDetails(loginType);
            Authentication authentication = authenticationManager.authenticate(token);
            loginUser = (LoginUser) authentication.getPrincipal();

            // 写入 Security 上下文并持久化到 session，后续请求由 SecurityFilterChain 识别登录态
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        } catch (AuthenticationException ex) {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("message", "doctor".equalsIgnoreCase(loginType)
                    ? "医生姓名或密码错误" : "用户名或密码错误");
            mv.addObject("username", username);
            mv.addObject("loginType", loginType);
            mv.addObject("redirect", redirect);
            return mv;
        }

        // 保留原会话属性：模板导航、LoginInterceptor 与既有测试都依赖它
        session.setAttribute(LoginInterceptor.SESSION_USER, loginUser);

        // 医生固定进工作台；患者优先回跳到来之前的页面
        if (loginUser.isDoctor()) {
            return new ModelAndView("redirect:/doctor/appointments");
        }
        if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
            return new ModelAndView("redirect:" + redirect);
        }
        return new ModelAndView("redirect:/home");
    }

    /**
     * 退出登录。
     * <p>实际由 Spring Security 的 {@code LogoutFilter} 处理（见 {@code SecurityConfig} 的 logout 配置），
     * 本方法只在过滤器未匹配时兜底，行为与改造前一致：作废会话后回首页。</p>
     */
    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return new ModelAndView("redirect:/home");
    }
}
