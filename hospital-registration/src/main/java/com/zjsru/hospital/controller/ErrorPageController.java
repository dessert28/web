package com.zjsru.hospital.controller;

import com.zjsru.hospital.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 错误页入口。403 由 LoginInterceptor 内部 forward 过来，其余由容器按 error 模板兜底。
 */
@Controller
public class ErrorPageController {

    @GetMapping("/forbidden")
    public ModelAndView forbidden(HttpSession session) {
        ModelAndView mv = new ModelAndView("error/403");
        mv.addObject("loginUser", session == null ? null : session.getAttribute(LoginInterceptor.SESSION_USER));
        return mv;
    }
}
