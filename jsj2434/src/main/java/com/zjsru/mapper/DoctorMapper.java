package com.zjsru.mapper;

import com.zjsru.domain.Doctor;
import org.apache.ibatis.annotations.Mapper;

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
}
