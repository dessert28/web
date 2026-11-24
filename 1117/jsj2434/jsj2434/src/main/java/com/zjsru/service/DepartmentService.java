package com.zjsru.service;

import com.zjsru.domain.Department;

public interface DepartmentService {
    Department queryDepartmentInfoAndDoctorsByDeptId(int deptId);
}
