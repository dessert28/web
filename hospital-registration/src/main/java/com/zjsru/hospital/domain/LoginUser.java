package com.zjsru.hospital.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存进 HttpSession 的登录态。
 * accountId 对患者是 tb_users.id，对医生是 doctor_info.id。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer accountId;
    private String displayName;
    private Role role;
    /** 医生登录时才有值，等于 doctor_info.id */
    private Integer doctorId;

    public boolean isDoctor() {
        return role == Role.DOCTOR;
    }

    public boolean isPatient() {
        return role == Role.PATIENT;
    }
}
