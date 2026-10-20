package com.zjsru.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zjsru.domain.User;
import org.junit.jupiter.api.Test;

/** 服务单元测试：直接创建实现，无需启动 Spring 容器即可验证账号规则。 */
class UserServiceTest {
    private final UserService userService = new UserServiceimpl();

    @Test
    // 无参构造和 setter 填充表单对象，验证唯一有效的演示账号。
    void acceptsDemoCredentials() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("123");
        assertTrue(userService.login(user));
    }

    @Test
    // 验证错误密码、其他用户名、已移除账号和空字段均不能登录。
    void rejectsOtherCredentials() {
        assertFalse(userService.login(new User("admin", "wrong")));
        assertFalse(userService.login(new User("guest", "123")));
        assertFalse(userService.login(new User("dessert", "202405315424")));
        assertFalse(userService.login(new User()));
    }
}
