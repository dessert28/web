package com.example.demo.controller;

import com.example.demo.model.ApiResult;
import com.example.demo.model.Users;
import com.example.demo.model.UserProfile;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/api")
public class UserApiController {
    private final UserService userService;

    public UserApiController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ApiResult register(@RequestBody(required = false) Users users) {
        try {
            if (users == null) throw new IllegalArgumentException("请求内容不能为空");
            byte[] avatar = decodeAvatar(users.getAvatarBase64());
            UserProfile profile = userService.register(users.getUsername(), users.getEmail(), users.getPassword(), users.getConfirmPassword(), avatar);
            return ApiResult.ok("注册成功", profile);
        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResult login(@RequestBody(required = false) Users users, HttpSession session) {
        if (users == null || !userService.authenticate(users.getUsername(), users.getPassword())) return ApiResult.fail("用户名或密码错误");
        UserProfile profile = userService.getProfile(users.getUsername());
        session.setAttribute(UserController.SESSION_USERNAME, profile.username());
        return ApiResult.ok("登录成功", profile);
    }

    @GetMapping("/me")
    public ApiResult me(HttpSession session) {
        Object username = session.getAttribute(UserController.SESSION_USERNAME);
        UserProfile profile = username == null ? null : userService.getProfile(username.toString());
        return profile == null ? ApiResult.fail("未登录") : ApiResult.ok(null, profile);
    }

    @PostMapping("/logout")
    public ApiResult logout(HttpSession session) {
        session.invalidate();
        return ApiResult.ok("已退出登录", null);
    }

    private byte[] decodeAvatar(String value) {
        if (value == null || value.isBlank()) return null;
        String encoded = value.contains(",") ? value.substring(value.indexOf(',') + 1) : value;
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("头像数据格式不正确");
        }
    }
}
