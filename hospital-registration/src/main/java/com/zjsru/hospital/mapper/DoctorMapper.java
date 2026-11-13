package com.zjsru.hospital.mapper;

import com.zjsru.hospital.domain.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医生数据访问（doctor_info）。
 * XML 在 resources/mapper/DoctorMapper.xml。
 */
@Mapper
public interface DoctorMapper {

    // 查询所有医生信息
    List<Doctor> queryAllDoctors();

    // 根据医生ID查询医生信息
    Doctor queryDoctorById(@Param("doctorid") Integer doctorId);

    // 添加医生信息
    int addDoctor(Doctor doctor);

    // 根据条件查询医生信息
    List<Doctor> queryDoctorsByCondition(Doctor doctor);

    // 根据单个条件查询医生信息
    List<Doctor> queryDoctorsBySingleCondition(Doctor doctor);

    // 动态更新医生信息：只更新传入的非空字段
    int updateDoctor(Doctor doctor);

    // 根据多个医生ID批量查询
    List<Doctor> queryDoctorsByIds(@Param("ids") List<Integer> ids);

    // 医生登录：姓名 + 密码
    Doctor queryDoctorByNameAndPassword(@Param("doctorName") String doctorName,
                                       @Param("password") String password);

    // 按姓名/科室/专长模糊检索医生（供科室检索联动）
    List<Doctor> queryDoctorsByKeyword(@Param("keyword") String keyword);

    // 该科室下的医生（按 id 排序）
    List<Doctor> queryDoctorsByDeptId(@Param("deptId") Integer deptId);
}
