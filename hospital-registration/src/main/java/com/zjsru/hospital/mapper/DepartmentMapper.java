package com.zjsru.hospital.mapper;

import com.zjsru.hospital.domain.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科室数据访问（doctor_department）。
 * XML 在 resources/mapper/DepartmentMapper.xml。
 */
@Mapper
public interface DepartmentMapper {

    // 查询全部科室（不含医生）
    List<Department> queryAllDepartments();

    // 根据科室ID查询科室信息及其下属全部医生（一对多关联查询）
    Department queryDepartmentInfoAndDoctorsByDeptId(@Param("deptId") Integer deptId);

    // 单个科室（不含医生）
    Department queryDepartmentById(@Param("deptId") Integer deptId);

    // 按关键字检索科室：科室名 / 位置 / 电话
    List<Department> queryDepartmentsByKeyword(@Param("keyword") String keyword);
}
