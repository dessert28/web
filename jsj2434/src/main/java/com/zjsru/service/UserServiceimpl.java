package com.zjsru.service;

import com.zjsru.domain.User;
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
