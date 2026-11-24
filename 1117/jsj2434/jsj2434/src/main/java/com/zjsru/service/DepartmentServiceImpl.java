package com.zjsru.service;

import com.zjsru.domain.Department;
import com.zjsru.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService  {
    @Autowired
    DepartmentMapper departmentMapper;
    @Override
    public Department queryDepartmentInfoAndDoctorsByDeptId(int deptId) {
        return departmentMapper.queryDepartmentInfoAndDoctorsByDeptId(deptId);
    }
}
