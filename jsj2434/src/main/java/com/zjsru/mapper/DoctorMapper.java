package com.zjsru.mapper;

import com.zjsru.domain.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {
    // 查询所有医生信息
    public List<Doctor> queryAllDoctors();
    // 根据医生ID查询医生信息
    public Doctor queryDoctorById(int doctorid);
    //添加医生信息
    public int addDoctor(Doctor doctor);
    // 根据条件查询医生信息
    public List<Doctor> queryDoctorsByCondition(Doctor doctor);
    // 根据单个条件查询医生信息
    public List<Doctor> queryDoctorsBySingleCondition(Doctor doctor);
    // 动态更新医生信息：只更新传入的非空字段
    public int updateDoctor(Doctor doctor);
    // 根据多个医生ID批量查询
    public List<Doctor> queryDoctorsByIds(@Param("ids") List<Integer> ids);
}
