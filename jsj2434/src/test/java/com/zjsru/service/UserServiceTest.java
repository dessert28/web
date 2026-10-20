package com.zjsru.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zjsru.domain.Users;
import org.junit.jupiter.api.Test;

/** 服务单元测试：直接创建实现，无需启动 Spring 容器即可验证账号规则。 */
class UserServiceTest {
    private final UserService userService = new UserServiceimpl();

    @Test
    // 无参构造和 setter 填充表单对象，验证唯一有效的演示账号。
    void acceptsDemoCredentials() {
        Users user = new Users();
        user.setUsername("admin");
        user.setPassword("123");
        assertTrue(userService.login(user));
    }

    @Test
    // 验证错误密码、其他用户名、已移除账号和空字段均不能登录。
    void rejectsOtherCredentials() {
        assertFalse(userService.login(credentials("admin", "wrong")));
        assertFalse(userService.login(credentials("guest", "123")));
        assertFalse(userService.login(credentials("dessert", "202405315424")));
        assertFalse(userService.login(new Users()));
    }

    // @Data 未生成全参构造，通过 setter 组装测试账号。
    private Users credentials(String username, String password) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}
