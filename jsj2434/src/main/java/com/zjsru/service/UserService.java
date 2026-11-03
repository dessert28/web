package com.zjsru.service;

import com.zjsru.domain.Users;

import java.util.List;

public interface UserService {
    Boolean login(Users user);
    //查询所有用户
    List<Users> selectAllUsers();
    Users queryUserById(Integer id);
    Users queryUserByNameAndPassword(String username,String password);
}
