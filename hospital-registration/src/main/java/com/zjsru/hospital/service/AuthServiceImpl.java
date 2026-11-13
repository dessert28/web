package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Doctor;
import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.Role;
import com.zjsru.hospital.domain.UserCredential;
import com.zjsru.hospital.domain.Users;
import com.zjsru.hospital.mapper.DoctorMapper;
import com.zjsru.hospital.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录实现（课程演示：密码明文比较，不做加密与注册）。
 * <ul>
 *   <li>患者：tb_users.username + password</li>
 *   <li>医生：doctor_info.doctorName + password（password 列由 db/experiment2-appointment.sql 追加，默认 123）</li>
 * </ul>
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public LoginUser login(UserCredential credential) {
        if (credential == null
                || credential.getUsername() == null || credential.getUsername().isBlank()
                || credential.getPassword() == null || credential.getPassword().isBlank()) {
            return null;
        }
        String username = credential.getUsername().trim();
        String password = credential.getPassword();

        if ("doctor".equalsIgnoreCase(credential.getLoginType())) {
            Doctor doctor = doctorMapper.queryDoctorByNameAndPassword(username, password);
            if (doctor == null) {
                return null;
            }
            return new LoginUser(doctor.getId(), doctor.getDoctorName(), Role.DOCTOR, doctor.getId());
        }

        Users patient = userMapper.selectUserByNameAndPassword(username, password);
        if (patient == null) {
            return null;
        }
        return new LoginUser(patient.getId(), patient.getUsername(), Role.PATIENT, null);
    }
}
