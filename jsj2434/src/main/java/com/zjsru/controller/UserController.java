package com.zjsru.controller;

import com.zjsru.domain.Users;
import com.zjsru.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 用户相关请求。
 *
 * <p>登录与注销已交给 Spring Security：</p>
 * <ul>
 *   <li>{@code POST /login} 由 {@code UsernamePasswordAuthenticationFilter} 处理，
 *       认证来源是 {@code DataBaseUserDeatilsService} 查的 {@code sys_user} 三表；</li>
 *   <li>{@code GET /logout} 由 {@code LogoutFilter} 处理；</li>
 *   <li>失败重定向到 {@code /toLogin?error}，注销成功重定向到 {@code /toLogin?logout}，
 *       本类只负责把这两个参数翻译成页面提示。</li>
 * </ul>
 *
 * <p>因此这里不再声明 {@code /login} 与 {@code /logout} 的处理方法，
 * 避免与过滤器形成两个入口。</p>
 */
@Controller
public class UserController {

    @Autowired
    UserService userService;

    //请求主页
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 登录页。
     *
     * @param error  非空表示上一次认证失败（Security 的 failureUrl）
     * @param logout 非空表示刚注销（Security 的 logoutSuccessUrl）
     */
    @RequestMapping("/toLogin")
    public ModelAndView toLogin(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout) {
        ModelAndView mv = new ModelAndView("login");
        if (error != null) {
            mv.addObject("message", "用户名或密码错误");
        }
        if (logout != null) {
            mv.addObject("message", "已安全退出");
        }
        return mv;
    }

    //模拟数据库查询用户信息，并跳转到主页面，且携带用户信息
    @RequestMapping("/toMain")
    public ModelAndView toMain(Users user, ModelAndView modelAndView) {
        modelAndView.setViewName("main");
        modelAndView.addObject("username", user.getUsername());
        return modelAndView;
    }

    //退出登录的提示页（真正的注销由 Security 的 /logout 完成）
    @RequestMapping("/logout2")
    public ModelAndView logout2(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        modelAndView.addObject("message", "退出成功");
        return modelAndView;
    }

    //测试Restful风格，处理不同请求方法的请求
    @RequestMapping(value = "/test/{name}/{password}", method = RequestMethod.GET)
    public ModelAndView test(@PathVariable String password, @PathVariable("name") String username,
                             ModelAndView modelAndView) {
        modelAndView.addObject("username", username);
        modelAndView.addObject("password", password);
        modelAndView.setViewName("main");
        return modelAndView;
    }

    //用户列表：登录后由 Security 的 defaultSuccessUrl 引导到这里
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
        return userService.queryUserById(id);
    }

    //根据用户名和密码查询用户信息
    @GetMapping("/queryUserByNameAndPassword/{username}/{password}")
    @ResponseBody
    public Users queryUserByNameAndPassword(@PathVariable("username") String username,
                                            @PathVariable("password") String password) {
        return userService.queryUserByNameAndPassword(username, password);
    }
}
