package com.zjsru.controller;

import com.zjsru.domain.User;
import com.zjsru.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    //请求主页
    @RequestMapping("/")
    public String index() {
        return "index";
    }
    //请求登录页面
    @RequestMapping("/tologin")
    public String tologin() {
        return "login";
    }
    //处理登录请求
    @RequestMapping("/login")
    public String login(String username, String password) {
        User user = new User(username, password);
        if (userService.login(user)) {
            return "forward:/toMain";
        } else {
            return "login";
        }
    }
    @RequestMapping("/toMain")
    public ModelAndView toMain(User user, ModelAndView modelAndView) {
        //模拟从数据库查询用户信息，这里简单地使用用户传入的用户名作为示例
        modelAndView.setViewName("main");
        modelAndView.addObject("username", user.getUsername());
        return modelAndView;
    }

    @RequestMapping("/logout")
    @ResponseBody
    public String logout() {
        return "退出成功";
    }
    @RequestMapping("/logout2")
    public ModelAndView logout2(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        modelAndView.addObject("message", "退出成功");
        return modelAndView;
    }

}
