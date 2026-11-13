package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Doctor;
import com.zjsru.hospital.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<Doctor> queryAllDoctors() {
        return doctorMapper.queryAllDoctors();
    }

    @Override
    public Doctor queryDoctorById(Integer id) {
        if (id == null) {
            return null;
        }
        return doctorMapper.queryDoctorById(id);
    }

    @Override
    public List<Doctor> queryDoctorsByDeptId(Integer deptId) {
        if (deptId == null) {
            return List.of();
        }
        return doctorMapper.queryDoctorsByDeptId(deptId);
    }

    @Override
    public List<Doctor> queryDoctorsByCondition(Doctor condition) {
        return doctorMapper.queryDoctorsByCondition(condition);
    }

    @Override
    public int addDoctor(Doctor doctor) {
        return doctorMapper.addDoctor(doctor);
    }
}
