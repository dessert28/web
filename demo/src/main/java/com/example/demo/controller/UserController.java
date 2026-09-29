package com.example.demo.controller;

import com.example.demo.model.UserProfile;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {
    public static final String SESSION_USERNAME = "LOGIN_USERNAME";
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping({"/", "/login"})
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "registered", required = false) String registered,
                            @RequestParam(name = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "用户名或密码错误");
        if (registered != null) model.addAttribute("message", "注册成功，请登录");
        if (logout != null) model.addAttribute("message", "你已安全退出");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "username", defaultValue = "") String username,
                        @RequestParam(name = "password", defaultValue = "") String password,
                        HttpSession session) {
        if (!userService.authenticate(username, password)) return "redirect:/login?error";
        session.setAttribute(SESSION_USERNAME, username.trim());
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam(name = "username", defaultValue = "") String username,
                           @RequestParam(name = "email", defaultValue = "") String email,
                           @RequestParam(name = "password", defaultValue = "") String password,
                           @RequestParam(name = "confirmPassword", defaultValue = "") String confirmPassword,
                           @RequestParam(name = "avatar", required = false) MultipartFile avatar,
                           Model model) {
        try {
            byte[] avatarBytes = avatar == null || avatar.isEmpty() ? null : avatar.getBytes();
            userService.register(username, email, password, confirmPassword, avatarBytes);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Object username = session.getAttribute(SESSION_USERNAME);
        if (username == null) return "redirect:/login";
        UserProfile profile = userService.getProfile(username.toString());
        if (profile == null) {
            session.invalidate();
            return "redirect:/login";
        }
        model.addAttribute("profile", profile);
        return "home";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
