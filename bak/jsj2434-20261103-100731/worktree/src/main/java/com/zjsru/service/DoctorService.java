package com.zjsru.service;

import com.zjsru.domain.Doctor;

import java.util.List;

/** 医生业务契约，使控制器通过接口调用医生数据访问实现。 */
public interface DoctorService {
    /** 查询 doctor_info 表中的全部医生。 */
    List<Doctor> queryAllDoctors();

    /** 按主键查询单个医生。 */
    Doctor queryDoctorById(Integer id);

    /** 按姓名查询医生，同名的可能有多条。 */
    List<Doctor> queryDoctorsByName(String doctorName);

    /** 新增医生，返回受影响行数。 */
    int insertDoctor(Doctor doctor);

    /** 按主键更新医生，返回受影响行数。 */
    int updateDoctor(Doctor doctor);

    /** 按主键删除医生，返回受影响行数。 */
    int deleteDoctorById(Integer id);
}
