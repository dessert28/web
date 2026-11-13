package com.zjsru.hospital.domain;

/**
 * 登录角色。患者走 tb_users，医生走 doctor_info。
 */
public enum Role {
    /** 患者：首页浏览、预约挂号、取消预约 */
    PATIENT,
    /** 医生：查看本人就诊预约 */
    DOCTOR;

    public String getLabel() {
        return this == DOCTOR ? "医生" : "患者";
    }
}
