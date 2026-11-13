package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Department;
import com.zjsru.hospital.domain.DepartmentSearchResult;

import java.util.List;

/**
 * 科室业务：浏览、检索、科室详情。
 */
public interface DepartmentService {

    /** 全部科室（不含医生），首页与后台共用 */
    List<Department> queryAllDepartments();

    /** 科室 + 下属医生；deptId 为空直接返回 null，未知科室返回 null */
    Department queryDepartmentInfoAndDoctorsByDeptId(Integer deptId);

    /** 单个科室（不含医生），未知科室返回 null */
    Department queryDepartmentById(Integer deptId);

    /**
     * 科室/门诊检索：先按科室名/位置/电话匹配科室，再按医生姓名/职称/专长匹配，
     * 命中医生的科室也会出现在结果里（并带上命中医生列表）。
     */
    List<DepartmentSearchResult> search(String keyword);
}
