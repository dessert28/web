package com.zjsru.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD_HASH = "$2b$10$rr4VGknqC2mKS6tPXaXhueuqawUT5AiAYlTq2qXSl0pbI4eBfBZEq";

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (!ADMIN_USERNAME.equals(username)) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return User.withUsername(ADMIN_USERNAME)
                .password(ADMIN_PASSWORD_HASH)
                .roles("USER")
                .build();
    }
}
