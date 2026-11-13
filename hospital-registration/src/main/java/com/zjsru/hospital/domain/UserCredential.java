package com.zjsru.hospital.domain;

import lombok.Data;

/**
 * 登录表单的提交载体。
 * loginType: patient（默认，查 tb_users）/ doctor（查 doctor_info）。
 */
@Data
public class UserCredential {
    private String username;
    private String password;
    private String loginType;
}
