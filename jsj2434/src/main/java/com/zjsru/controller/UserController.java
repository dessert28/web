package com.zjsru.controller;

import com.zjsru.domain.Users;
import com.zjsru.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/** 页面控制器：普通返回字符串表示模板名，@ResponseBody 方法则直接输出正文。 */
@Controller
public class UserController {
    // 注入 UserService 实现，将账号校验交给服务层。
    @Autowired
    private UserService userService;
    /** 返回欢迎页，index 根据配置解析为 templates/index.html。 */
    @RequestMapping("/")
    public String index() {
        return "index";
    }
    /** 返回登录表单页面。 */
    @RequestMapping("/tologin")
    public String tologin() {
        return "login";
    }
    /** 按参数名绑定表单 username、password，并调用服务校验账号。 */
    // @RequestMapping 未限定 HTTP 方法；当前页面使用 POST 提交。
    @RequestMapping("/login")
    public String login(String username, String password) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(password);
        if (userService.login(user)) {
            // 服务端内部转发并沿用请求参数，浏览器地址不会因转发改变。
            // 当前只校验账号，没有创建登录会话或向 session 写入 username。
            return "forward:/toMain";
        } else {
            return "login";
        }
    }
    /** 请求参数绑定到 Users，ModelAndView 同时指定模板名和模型数据。 */
    @RequestMapping("/toMain")
    public ModelAndView toMain(Users user, ModelAndView modelAndView) {
        // 仅使用请求参数中的用户名，没有连接数据库或查询用户资料。
        // 模型 username 与 session.username 不同，主页模板读取的是后者。
        modelAndView.setViewName("main");
        modelAndView.addObject("username", user.getUsername());
        return modelAndView;
    }

    /** 浏览器访问 /testException，故意触发整数除零，由全局处理器返回异常 JSON。 */
    @GetMapping("/testException")
    @ResponseBody
    public String testException() {
        int divisor = 0;
        return String.valueOf(10 / divisor);
    }

    /** @ResponseBody 返回演示用户对象，由 Spring 序列化为 JSON；这里没有销毁会话或执行认证注销。 */
    @RequestMapping("/logout")
    @ResponseBody
    public Users logout() {
        Users user = new Users();
        user.setUsername("张三");
        user.setPassword("123");
        return user;
    }
    /** 返回登录页，通过模型 message 显示退出提示；同样没有销毁会话。 */
    @RequestMapping("/logout2")
    public ModelAndView logout2(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        modelAndView.addObject("message", "退出成功");
        return modelAndView;
    }

    /** 测试 Restful 风格：路径占位符 name、password 分别绑定到方法参数。 */
    @RequestMapping(value = "/test/{name}/{password}", method = RequestMethod.GET)
    public ModelAndView test(@PathVariable("name") String username, @PathVariable("password") String password,
                             ModelAndView modelAndView) {
        modelAndView.addObject("username", username);
        modelAndView.addObject("password", password);
        modelAndView.setViewName("main");
        return modelAndView;
    }

    /** 查询 tb_users 表全部用户，@ResponseBody 将 List 序列化为 JSON 数组。 */
    @GetMapping("/selectAllUsers")
    @ResponseBody
    public List<Users> selectAllUsers() {
        //1.调用业务逻辑方法
        List<Users> usersList = userService.selectAllUsers();
        //2.返回结果给前端
        return usersList;
    }

    /** 按路径 id 查询单个用户，@ResponseBody 将对象序列化为 JSON。 */
    @GetMapping("/queryUserById/{id}")
    @ResponseBody
    public Users queryUserById(@PathVariable int id) {
        //1.调用业务逻辑方法
        Users users = userService.queryUserById(id);
        //2.返回结果给前端
        return users;
    }

    /** 根据用户名和密码查询用户信息，两个路径参数依次绑定。 */
    @GetMapping("/queryUserByNameAndPassword/{username}/{password}")
    @ResponseBody
    public Users queryUserByNameAndPassword(@PathVariable("username") String username, @PathVariable("password") String password) {
        //1.调用业务逻辑方法
        Users users = userService.queryUserByNameAndPassword(username, password);
        //2.返回结果给前端
        return users;
    }

    /** 新增用户：表单参数绑定到 Users，返回受影响行数。 */
    @PostMapping("/insertUser")
    @ResponseBody
    public int insertUser(Users user) {
        //1.调用业务逻辑方法
        int rows = userService.insertUser(user);
        //2.返回结果给前端
        return rows;
    }

    /** 修改用户：按 id 更新其余字段，返回受影响行数。 */
    @PutMapping("/updateUser")
    @ResponseBody
    public int updateUser(Users user) {
        //1.调用业务逻辑方法
        int rows = userService.updateUser(user);
        //2.返回结果给前端
        return rows;
    }

    /** 删除用户：按路径 id 删除，返回受影响行数。 */
    @DeleteMapping("/deleteUserById/{id}")
    @ResponseBody
    public int deleteUserById(@PathVariable Long id) {
        //1.调用业务逻辑方法
        int rows = userService.deleteUserById(id);
        //2.返回结果给前端
        return rows;
    }

}
