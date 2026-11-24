package com.zjsru.domain;

import lombok.Data;

import java.util.List;

@Data
public class Department {
    // 部门编号,部门名称，部门位置，部门电话
    private Integer deptId;
    private String deptName;
    private String location;
    private String deptPhone;
    //部门包含的医生列表
    private List<Doctor> doctorList;
}
