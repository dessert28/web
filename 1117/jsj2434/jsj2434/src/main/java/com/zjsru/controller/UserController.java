package com.zjsru.controller;

import com.zjsru.domain.Users;
import com.zjsru.service.UserService;
import com.zjsru.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author zjsru
 * @date 2023/04/03
 */
@Controller
//请求处理类，处理用户相关请求，并返回对应页面
public class UserController {
    @Autowired
    UserService userService;
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
        boolean result = userService.login(user);
        if (result) {
           // return "forward:/toMain";
            return "forward:/selectAllUsers";
        } else {
            return "login";
        }
    }
    //模拟数据库查询用户信息，并跳转到主页面，且携带用户信息
    @RequestMapping("/toMain")
    public ModelAndView toMain(Users user, ModelAndView modelAndView) {
        //模拟从数据库查询用户信息，这里简单地使用用户传入的用户名作为示例
        modelAndView.setViewName("main");
        modelAndView.addObject("username", user.getUsername());
        return modelAndView;
    }
    //处理退出登录请求
    @RequestMapping("/logout")
    @ResponseBody
    public Users logout() {
        Users user = new Users();
        user.setUsername("张三");
        user.setPassword("123");
        return user;
    }
    @RequestMapping("/logout2")
    public ModelAndView logout2(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        modelAndView.addObject("message", "退出成功");
        return modelAndView;
    }
    //测试Restful风格，处理不同请求方法的请求
    @RequestMapping(value = "/test/{name}/{password}", method = RequestMethod.GET)
    public ModelAndView test( @PathVariable String password,@PathVariable("name") String username, ModelAndView modelAndView) {
        modelAndView.addObject("username", username);
        modelAndView.addObject("password", password);
        modelAndView.setViewName("main");
        return modelAndView;
    }
    @RequestMapping("/selectAllUsers")
    public ModelAndView selectAllUsers(ModelAndView modelAndView) {
        //1.调用业务逻辑方法
        List<Users> usersList = userService.selectAllUsers();
        //2.返回结果给前端
        modelAndView.setViewName("main");
        modelAndView.addObject("usersList", usersList);
        return modelAndView;
    }
    @GetMapping("/queryUserById/{id}")
    @ResponseBody
    public Users queryUserById(@PathVariable int id) {
        //1.调用业务逻辑方法
        Users users = userService.queryUserById(id);
        //2.返回结果给前端
        return users;
    }
    //根据用户名和密码查询用户信息
    @GetMapping("/queryUserByNameAndPassword/{username}/{password}")
    @ResponseBody
    public Users queryUserByNameAndPassword(@PathVariable("username") String username, @PathVariable("password") String password) {
        //1.调用业务逻辑方法
        Users users = userService.queryUserByNameAndPassword(username, password);
        //2.返回结果给前端
        return users;
    }

}
