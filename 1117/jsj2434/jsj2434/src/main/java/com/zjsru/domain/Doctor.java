package com.zjsru.domain;

import lombok.Data;

import java.util.List;

@Data
public class Doctor {
    private Integer id;
    private String doctorName;
    private String gender;
    //private String department;
    private String title;
    private String phone;
    private Department department;
    //医生关联的患者列表
    private List<Patient> patientList;

}
