package com.example.demo;

import org.junit.jupiter.api.Test;
import com.example.demo.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UserControllerTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void validLoginReturnsMainPage() throws Exception {
        assertEquals("main", new UserController().login("1", "1"));
    }

    @Test
    void controllerIsRegisteredAsSpringBean() {
        assertNotNull(applicationContext.getBean(UserController.class));
    }
}
