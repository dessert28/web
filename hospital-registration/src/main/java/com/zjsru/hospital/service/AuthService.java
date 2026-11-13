package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.UserCredential;

/**
 * 登录业务：患者查 tb_users，医生查 doctor_info。
 */
public interface AuthService {

    /**
     * 校验账号密码。
     *
     * @return 成功返回登录态，失败返回 null
     */
    LoginUser login(UserCredential credential);
}
