package com.zjsru.mapper;

import com.zjsru.domain.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    // 查询全部科室（不含医生），用于科室列表页
    public List<Department> queryAllDepartments();

    // 根据科室ID查询科室信息及其下属全部医生（一对多关联查询）
    public Department queryDepartmentInfoAndDoctorsByDeptId(@Param("deptId") Integer deptId);
}
