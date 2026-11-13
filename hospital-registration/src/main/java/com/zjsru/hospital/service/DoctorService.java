package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Doctor;

import java.util.List;

/**
 * 医生业务：详情、科室联动、条件查询。
 */
public interface DoctorService {

    List<Doctor> queryAllDoctors();

    /** 医生详情；id 为空或不存在返回 null */
    Doctor queryDoctorById(Integer id);

    List<Doctor> queryDoctorsByDeptId(Integer deptId);

    List<Doctor> queryDoctorsByCondition(Doctor condition);

    int addDoctor(Doctor doctor);
}
