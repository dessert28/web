package com.zjsru.service;

import com.zjsru.domain.Doctor;

import java.util.List;

public interface DoctorService {
    public List<Doctor> queryAllDoctors();
    public Doctor queryDoctorById(int id);
    public int addDoctor(Doctor doctor);
    // 根据条件查询医生信息
    public List<Doctor> queryDoctorsByCondition(Doctor doctor);
    // 根据单个条件查询医生信息
    public List<Doctor> queryDoctorsBySingleCondition(Doctor doctor);
    // 动态更新医生信息
    public int updateDoctor(Doctor doctor);
    // 根据多个医生ID批量查询
    public List<Doctor> queryDoctorsByIds(List<Integer> ids);
}
