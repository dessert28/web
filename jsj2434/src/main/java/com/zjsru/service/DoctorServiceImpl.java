package com.zjsru.service;

import com.zjsru.domain.Doctor;
import com.zjsru.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** @Service 将实现类注册到 Spring 容器，供控制器注入。 */
@Service
public class DoctorServiceImpl implements DoctorService {
    // 注入 MyBatis Mapper，由 MyBatis 生成代理实现。
    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<Doctor> queryAllDoctors() {
        List<Doctor> doctors = doctorMapper.queryAllDoctors();
        return doctors;
    }

    @Override
    public Doctor queryDoctorById(Integer id) {
        return doctorMapper.queryDoctorById(id);
    }

    @Override
    public List<Doctor> queryDoctorsByName(String doctorName) {
        return doctorMapper.queryDoctorsByName(doctorName);
    }

    @Override
    public int insertDoctor(Doctor doctor) {
        return doctorMapper.insertDoctor(doctor);
    }

    @Override
    public int updateDoctor(Doctor doctor) {
        return doctorMapper.updateDoctor(doctor);
    }

    @Override
    public int deleteDoctorById(Integer id) {
        return doctorMapper.deleteDoctorById(id);
    }
}
