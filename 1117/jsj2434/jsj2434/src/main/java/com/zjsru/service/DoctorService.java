package com.zjsru.service;

import com.zjsru.domain.Doctor;

import java.util.List;

public interface DoctorService {
    public List<Doctor> queryAllDoctors();
    public Doctor queryDoctorById(int id);
    public int addDoctor(Doctor doctor);
    public List<Doctor> queryDoctorsByCondition(Doctor doctor);
    public List<Doctor> queryDoctorsBySingleCondition(Doctor doctor);
    public Doctor queryDoctorInfoAndDepartmentInfo(int id);
}
