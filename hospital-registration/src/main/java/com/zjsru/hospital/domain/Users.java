package com.zjsru.hospital.domain;

import lombok.Data;

/**
 * 患者账号，对应 tb_users 表（与 jsj2434 共用）。
 * rolename 里的 admin / ROLE_common / ROLE_vip 统一映射成患者角色。
 */
@Data
public class Users {
    private Integer id;
    private String username;
    private String password;
    private String rolename;
    private String status;
    private String phone;
}
