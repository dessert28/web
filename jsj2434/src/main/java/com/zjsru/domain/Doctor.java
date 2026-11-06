package com.zjsru.domain;

import lombok.Data;

@Data
public class Doctor {
    private Integer id;
    private String doctorName;
    private String gender;
    // 入库用：对应 doctor_info.department_id，外键指向 department.id
    private Integer departmentId;
    // 展示用：不是 doctor_info 的列，由 DoctorMapper.xml 的 left join 从 department.dept_name 取回。
    // 模板 doctorList.html / doctorQuery.html 读的就是这个字段，所以名字保持 department 不变。
    private String department;
    private String title;
    private String phone;

}
