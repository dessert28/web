package com.zjsru.domain;

import lombok.Data;

/** 用户实体：对应 mybatis 库的 tb_users 登录表；Lombok 生成访问器。 */
@Data
public class Users {
    // 登录表主键，仅用于查询结果输出，登录校验不使用。
    private Long id;
    // 与表单输入框的 name 同名，用于绑定请求参数。
    private String username;
    // 仅用于演示账号校验，当前按明文比较。
    private String password;
    // 登录表扩展字段：角色名、状态、手机号，登录流程暂未使用。
    private String rolename;
    private String status;
    private String phone;
}
