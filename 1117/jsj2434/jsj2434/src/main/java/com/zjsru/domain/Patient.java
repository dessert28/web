package com.zjsru.domain;

import java.util.List;

public class Patient {
    private int patientId;
    private String patientName;
    private String gender;
    private String patientPhone;
    private String birthDate;
    //患者关联的医生列表
    private List<Doctor> doctorList; //患者关联的医生列表

}
