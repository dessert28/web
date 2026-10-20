package com.zjsru.service;

import com.zjsru.domain.Users;
import com.zjsru.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** @Service 将实现类注册到 Spring 容器，供控制器注入。 */
@Service
public class UserServiceimpl implements UserService {
    // 注入 MyBatis Mapper，由 MyBatis 生成代理实现。
    @Autowired
    private UserMapper userMapper;

    /** 仅校验固定演示账号，不查询数据库或维护登录会话。 */
    @Override
    public Boolean login(Users user) {
        // 常量调用 equals 可处理字段为 null 的情况；user 对象本身仍须非 null。
        // 此固定账号仅供课程演示。
        if ("admin".equals(user.getUsername()) && "123".equals(user.getPassword())) {
            return true;
        } else {
            return false;
        }
    }

    /** 通过 MyBatis Mapper 查询 users 表中的全部记录。 */
    @Override
    public List<Users> selectAllUsers() {
        List<Users> users = userMapper.selectAllUsers();
        return users;
    }
}
