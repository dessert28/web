package com.zjsru.hospital.domain;

import lombok.Data;

/**
 * 科室实体，对应 doctor_department 表。
 * 一个科室关联多名医生，doctorList 由 DepartmentMapper.xml 的 resultMap 通过
 * &lt;collection&gt; 一次性映射出来（一对多关联查询）。
 */
@Data
public class Department {
    private Integer id;
    // 科室名称
    private String deptName;
    // 科室位置
    private String location;
    // 科室电话
    private String deptPhone;
    // 科室下的医生集合
    private java.util.List<Doctor> doctorList;
}
