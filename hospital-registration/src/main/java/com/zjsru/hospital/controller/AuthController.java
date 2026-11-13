package com.zjsru.hospital.controller;

import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.UserCredential;
import com.zjsru.hospital.interceptor.LoginInterceptor;
import com.zjsru.hospital.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录 / 退出。
 * 患者查 tb_users，医生查 doctor_info（password 列默认 123），全部走数据库比对。
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

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
        UserCredential credential = new UserCredential();
        credential.setUsername(username);
        credential.setPassword(password);
        credential.setLoginType(loginType);

        LoginUser loginUser = authService.login(credential);
        if (loginUser == null) {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("message", "doctor".equalsIgnoreCase(loginType)
                    ? "医生姓名或密码错误" : "用户名或密码错误");
            mv.addObject("username", username);
            mv.addObject("loginType", loginType);
            mv.addObject("redirect", redirect);
            return mv;
        }

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

    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        return new ModelAndView("redirect:/home");
    }
}
