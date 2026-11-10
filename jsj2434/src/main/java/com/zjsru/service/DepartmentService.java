package com.zjsru.service;

import com.zjsru.domain.Department;

import java.util.List;

public interface DepartmentService {
    // 查询全部科室
    public List<Department> queryAllDepartments();

    // 查询科室信息及其下属全部医生（一对多关联查询）
    public Department queryDepartmentInfoAndDoctorsByDeptId(Integer deptId);
}
