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

}
