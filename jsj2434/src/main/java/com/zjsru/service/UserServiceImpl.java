package com.zjsru.service;

import com.zjsru.domain.Users;
import com.zjsru.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired // Spring框架会自动将UserMapper接口的实现类注入到userMapper变量中
    private UserMapper userMapper;

    @Override
    public List<Users> selectAllUsers() {
        List<Users> users = userMapper.selectAllUsers();
        return users;
    }

    @Override
    public Users queryUserById(Integer id) {
        Users users = userMapper.selectUserById(id);
        return users;
    }

    @Override
    public Users queryUserByNameAndPassword(String username, String password) {
       return userMapper.selectUserByNameAndPassword(username, password);
    }
}
