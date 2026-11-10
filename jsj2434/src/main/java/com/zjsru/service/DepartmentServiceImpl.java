package com.zjsru.service;

import com.zjsru.domain.Department;
import com.zjsru.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> queryAllDepartments() {
        return departmentMapper.queryAllDepartments();
    }

    @Override
    public Department queryDepartmentInfoAndDoctorsByDeptId(Integer deptId) {
        // deptId 为空时直接返回 null，不做无意义的数据库往返
        if (deptId == null) {
            return null;
        }
        return departmentMapper.queryDepartmentInfoAndDoctorsByDeptId(deptId);
    }
}
