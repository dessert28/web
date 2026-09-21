package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @RequestMapping("/login")
    public String login(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password) {
        if ("1".equals(username) && "1".equals(password)) {
            return "main";
        }
        return "login";
    }

    @RequestMapping("/logout")
    public String logout() {
        return "logout success";
    }
}
