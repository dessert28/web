package com.zjsru.service;

import com.zjsru.domain.Users;
import com.zjsru.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/** @Service 将实现类注册到 Spring 容器，供控制器注入。 */
@Service
public class UserServiceimpl implements UserService {
    // 注入 MyBatis Mapper，由 MyBatis 生成代理实现。
    @Autowired
    private UserMapper userMapper;

    /** 按用户名查询 tb_users 表，并比较数据库中的密码。 */
    @Override
    public Boolean login(Users user) {
        // 按用户名查询用户；不存在时返回 null。
        Users stored = userMapper.selectByUsername(user.getUsername());
        // 使用 Objects.equals 兼容数据库密码或表单密码为空的情况。
        return stored != null && Objects.equals(stored.getPassword(), user.getPassword());
    }

    /** 通过 MyBatis Mapper 查询 tb_users 表中的全部记录。 */
    @Override
    public List<Users> selectAllUsers() {
        List<Users> users = userMapper.selectAllUsers();
        return users;
    }
    @Override
    public Users selectUserById(Integer id) {
        Users users = userMapper.selectUserById(id);
        return users;
    }
    @Override
    public Users queryUserByName(String username) {
        Users users = userMapper.selectByUsername(username);
        return users;
    }
    @Override
    public Users queryUserByNameAndPassword(String username, String password) {
        Users users = userMapper.selectUserByNameAndPassword(username, password);
        return users;
    }
}
