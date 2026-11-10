package com.example.demo.service;

import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceimpl implements UserService {
    @Override
    public Boolean login(User user) {
        if ("admin".equals(user.getUsername()) && "123".equals(user.getPassword())) {
            return true;
        } else {
            return false;
        }
    }
} 
