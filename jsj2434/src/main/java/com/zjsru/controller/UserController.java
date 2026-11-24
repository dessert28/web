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
 * <p>登录与注销由 Spring Security 接管：</p>
 * <ul>
 *   <li>{@code GET /login}（兼容 {@code /toLogin}）只负责渲染登录表单；</li>
 *   <li>{@code POST /login} 由 {@code UsernamePasswordAuthenticationFilter} 处理，
 *       认证来源是 {@code DataBaseUserDeatilsService} 查的 {@code sys_user} 三表；</li>
 *   <li>{@code GET/POST /logout} 由 {@code LogoutFilter} 处理；</li>
 *   <li>失败重定向到 {@code /login?error}，注销成功重定向到 {@code /login?logout}，
 *       本类只负责把这两个参数翻译成页面提示。</li>
 * </ul>
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
    @RequestMapping(value = {"/login", "/toLogin"}, method = RequestMethod.GET)
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
