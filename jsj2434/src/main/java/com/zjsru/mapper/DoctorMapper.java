package com.zjsru.mapper;

import com.zjsru.domain.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 医生 DAO：对应 mybatis 库的 doctor_info 表；启动类未加 @MapperScan，所以必须标 @Mapper 才会被注册。
    本接口不写注解 SQL，全部 SQL 位于 resources/mapper/DoctorMapper.xml，
    因此方法名必须与 XML 中 <select>/<insert>/<update>/<delete> 的 id 完全一致。 */
@Mapper
public interface DoctorMapper {
    // 查询所有医生信息
    List<Doctor> queryAllDoctors();

    // 根据医生ID查询医生信息
    Doctor queryDoctorById(@Param("id") Integer id);

    // 按医生姓名查询信息（同名的可能有多条，用 List 接收）
    List<Doctor> queryDoctorsByName(@Param("doctorName") String doctorName);

    // 新增医生（id 自增不显式写入），返回受影响行数
    int insertDoctor(Doctor doctor);

    // 按主键更新医生全部字段，返回受影响行数
    int updateDoctor(Doctor doctor);

    // 按主键删除医生，返回受影响行数
    int deleteDoctorById(@Param("id") Integer id);
}
