package com.zjsru.domain;

import lombok.Data;

@Data
public class Doctor {
    private Integer id;
    private String doctorName;
    private String gender;
    private String department;
    private String title;
    private String phone;
    //所属科室ID，关联 doctor_department.id（doctor_info.department_id 外键）
    private Integer departmentId;
    //所属科室对象，由 DepartmentMapper.xml 的 <association> 映射（容器内嵌 doctor 时使用）
    private Department dept;

}
