package com.zjsru.service;

import com.zjsru.domain.Users;

import java.util.List;

/** 登录业务契约，使控制器通过接口调用账号校验实现。 */
public interface UserService {
    /** 用户名和密码匹配数据库中的用户时返回 true，否则返回 false。 */
    Boolean login(Users user);

    /** 查询 users 表中的全部用户。 */
    List<Users> selectAllUsers();
}
