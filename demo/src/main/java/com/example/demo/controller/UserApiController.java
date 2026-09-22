package com.example.demo.controller;

import com.example.demo.model.ApiResult;
import com.example.demo.model.Users;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JSON 接口：请求体统一用 Users 对象接收，和页面流程共用同一个 UserService 与 Session。
 */
@RestController
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResult register(@RequestBody(required = false) Users users) {
        String username = users == null ? "" : users.getUsername();
        String password = users == null ? "" : users.getPassword();
        String confirmPassword = users == null ? "" : users.getConfirmPassword();

        String error = userService.register(username, password, confirmPassword);
        if (error != null) {
            return ApiResult.fail(error);
        }
        return ApiResult.ok("注册成功", username.trim());
    }

    @PostMapping("/login")
    public ApiResult login(@RequestBody(required = false) Users users, HttpSession session) {
        String username = users == null ? "" : users.getUsername();
        String password = users == null ? "" : users.getPassword();

        if (!userService.authenticate(username, password)) {
            return ApiResult.fail("用户名或密码错误");
        }
        String name = username.trim();
        session.setAttribute(UserController.SESSION_USERNAME, name);
        return ApiResult.ok("登录成功", name);
    }

    @GetMapping("/me")
    public ApiResult me(HttpSession session) {
        Object username = session.getAttribute(UserController.SESSION_USERNAME);
        if (username == null) {
            return ApiResult.fail("未登录");
        }
        return ApiResult.ok(null, username.toString());
    }

    @PostMapping("/logout")
    public ApiResult logout(HttpSession session) {
        session.invalidate();
        return ApiResult.ok("已退出登录", null);
    }
}
