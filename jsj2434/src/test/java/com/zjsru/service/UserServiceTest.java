package com.zjsru.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zjsru.domain.User;
import org.junit.jupiter.api.Test;

class UserServiceTest {
    private final UserService userService = new UserServiceimpl();

    @Test
    void acceptsDemoCredentials() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("123");
        assertTrue(userService.login(user));
    }

    @Test
    void rejectsOtherCredentials() {
        assertFalse(userService.login(new User("admin", "wrong")));
        assertFalse(userService.login(new User("guest", "123")));
        assertFalse(userService.login(new User("dessert", "202405315424")));
        assertFalse(userService.login(new User()));
    }
}
