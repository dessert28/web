package com.zjsru.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zjsru.domain.Users;
import com.zjsru.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 服务单元测试：用 Mockito 替换 Mapper，不启动 Spring 容器或连接真实数据库。 */
class UserServiceTest {
    private final UserMapper userMapper = mock(UserMapper.class);
    private final UserService userService = new UserServiceimpl();

    @BeforeEach
    // 字段注入的 Mapper 用 Spring 测试工具替换为 Mock 对象。
    void setUp() {
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
    }

    @Test
    // 数据库返回同名同密码用户时登录成功，不再依赖固定演示账号。
    void acceptsStoredCredentials() {
        when(userMapper.selectByUsername("admin")).thenReturn(credentials("admin", "123"));
        assertTrue(userService.login(credentials("admin", "123")));
    }

    @Test
    // 密码不一致、用户不存在或用户名为空时均不能登录。
    void rejectsInvalidCredentials() {
        when(userMapper.selectByUsername("admin")).thenReturn(credentials("admin", "123"));
        assertFalse(userService.login(credentials("admin", "wrong")));
        assertFalse(userService.login(credentials("guest", "123")));
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
