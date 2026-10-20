package com.zjsru.domain;

import lombok.Data;

/** 用户实体：保存账号、密码及联系方式；Lombok 生成访问器。 */
@Data
public class Users {
    // 与表单输入框的 name 同名，用于绑定请求参数。
    private String username;
    // 仅用于演示账号校验，当前示例没有数据库存储。
    private String password;
    // 以下联系方式字段为数据库扩展预留，登录流程暂未使用。
    private String email;
    private String phone;
    private String address;
}
