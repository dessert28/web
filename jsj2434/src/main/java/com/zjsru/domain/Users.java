package com.zjsru.domain;

import lombok.Data;

@Data
public class Users {
    private String id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
    //sys_user.enabled 的映射字段，供 Spring Security 判断是否禁用账号
    private Boolean status;

}
