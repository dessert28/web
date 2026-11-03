package com.zjsru.service;

import com.zjsru.domain.Doctor;
import com.zjsru.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class DoctorServiceImpl implements DoctorService {
    @Autowired
    DoctorMapper doctorMapper;
    @Override
    public List<Doctor> queryAllDoctors() {
        List<Doctor> doctors = doctorMapper.queryAllDoctors();
        return doctors;
    }

    @Override
    public Doctor queryDoctorById(int id) {
        return doctorMapper.queryDoctorById(id);
    }

    @Override
    public int addDoctor(Doctor doctor) {
        return doctorMapper.addDoctor(doctor);
    }

    @Override
    public List<Doctor> queryDoctorsByCondition(Doctor doctor) {
        return doctorMapper.queryDoctorsByCondition(doctor);
    }

    @Override
    public List<Doctor> queryDoctorsBySingleCondition(Doctor doctor) {
        return doctorMapper.queryDoctorsBySingleCondition(doctor);
    }
}
