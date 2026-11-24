package com.zjsru.service;

import com.zjsru.domain.Users;

import java.util.List;

public interface UserService {
    //查询所有用户
    List<Users> selectAllUsers();
    Users queryUserById(Integer id);
    Users queryUserByNameAndPassword(String username,String password);
}
