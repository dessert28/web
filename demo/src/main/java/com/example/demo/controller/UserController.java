package com.example.demo.controller;

import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UserController {

    public static final String SESSION_USERNAME = "LOGIN_USERNAME";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String registered,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        if (registered != null) {
            model.addAttribute("message", "注册成功，请登录");
        }
        if (logout != null) {
            model.addAttribute("message", "你已安全退出");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password,
            HttpSession session) {
        if (!userService.authenticate(username, password)) {
            return "redirect:/login?error";
        }
        session.setAttribute(SESSION_USERNAME, username.trim());
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password,
            @RequestParam(defaultValue = "") String confirmPassword,
            Model model) {
        String error = userService.register(username, password, confirmPassword);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("username", username);
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Object username = session.getAttribute(SESSION_USERNAME);
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "home";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
