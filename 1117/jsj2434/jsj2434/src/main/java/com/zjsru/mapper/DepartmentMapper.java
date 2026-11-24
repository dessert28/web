package com.zjsru.mapper;

import com.zjsru.domain.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper {
    //查询部门信息及部门下的医生信息
    public Department queryDepartmentInfoAndDoctorsByDeptId(int deptId);
}
