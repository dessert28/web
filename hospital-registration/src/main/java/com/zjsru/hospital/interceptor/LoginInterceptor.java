package com.zjsru.hospital.interceptor;

import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器。
 * <ul>
 *   <li>未登录 → 302 到 /toLogin?redirect=原路径</li>
 *   <li>患者访问医生端（/doctor/**）→ 403 页面</li>
 * </ul>
 * 首页、检索、登录页等不登记在拦截路径里，允许匿名访问。
 */
public class LoginInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "LOGIN_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser loginUser = session == null ? null : (LoginUser) session.getAttribute(SESSION_USER);

        if (loginUser == null) {
            String target = request.getRequestURI();
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) {
                target = target + "?" + query;
            }
            String encoded = URLEncoder.encode(target, StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/toLogin?redirect=" + encoded);
            return false;
        }

        // 医生端只允许医生进入
        String uri = request.getRequestURI();
        if (uri.startsWith(request.getContextPath() + "/doctor/") && loginUser.getRole() != Role.DOCTOR) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.getRequestDispatcher("/forbidden").forward(request, response);
            return false;
        }
        return true;
    }

    /**
     * 未登录时给 /forbidden 之前的请求补上登录态，让 403 页能正常显示导航。
     * （这里不做拦截，仅透出模型属性。）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null && modelAndView.getModel().get("loginUser") == null) {
            modelAndView.addObject("loginUser", session.getAttribute(SESSION_USER));
        }
    }
}
