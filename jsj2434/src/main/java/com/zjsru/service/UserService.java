package com.zjsru.service;

import com.zjsru.domain.User;

/** 登录业务契约，使控制器通过接口调用账号校验实现。 */
public interface UserService {
    /** 用户名和密码匹配演示账号时返回 true，否则返回 false。 */
    Boolean login(User user);
}
