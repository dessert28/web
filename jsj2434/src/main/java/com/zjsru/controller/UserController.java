package com.zjsru.controller;

import com.zjsru.domain.Users;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * @author zjsru
 * @date 2023/04/03
 */
@Controller
public class UserController {
    //请求主页(GetMapping,PostMapping,PutMapping,DeleteMapping)
    @RequestMapping("/")
    public String index() {
        return "index";
    }
    //请求登录页面
    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }
    //处理登录请求
    @RequestMapping("/login")
    public String login(Users user) {
        if ("admin".equals(user.getUsername()) && "123".equals(user.getPassword())) {
            return "main";
        } else {
            return "login";
        }
    }
}
