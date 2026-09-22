package com.example.demo.service;

import com.example.demo.model.UserAccount;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(String username, String password, String confirmPassword) {
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 20) {
            return "用户名长度必须为 3–20 位";
        }
        if (password == null || password.length() < 6) {
            return "密码至少需要 6 位";
        }
        if (!password.equals(confirmPassword)) {
            return "两次密码不一致";
        }
        UserAccount account = new UserAccount(normalizedUsername, passwordEncoder.encode(password));
        return users.putIfAbsent(normalizedUsername, account) == null ? null : "用户名已存在";
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        UserAccount account = users.get(username.trim());
        return account != null && passwordEncoder.matches(password, account.passwordHash());
    }
}
