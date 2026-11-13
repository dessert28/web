package com.zjsru.hospital.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约挂号记录，对应 tb_appointment 表。
 * 除表字段外，还带若干只读展示字段（医生姓名/科室/职称/就诊人标签），
 * 由 AppointmentMapper.xml 在查询时 JOIN 出来，不参与插入。
 */
@Data
public class Appointment {
    private Integer id;
    private Integer doctorId;
    private Integer userId;
    private String patientName;
    private String patientGender;
    private String patientPhone;
    private String patientIdCard;
    private LocalDate visitDate;
    private String timeSlot;
    /** 0-待就诊，1-已取消 */
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

    // ---------- JOIN 出来的展示字段（只读） ----------
    private String doctorName;
    private String doctorTitle;
    private String deptName;

    public boolean isCancelled() {
        return status != null && status == 1;
    }

    public String getStatusLabel() {
        return isCancelled() ? "已取消" : "待就诊";
    }

    /** 挂号时的就诊人描述，如「张伟（男）」 */
    public String getPatientLabel() {
        if (patientGender == null || patientGender.isBlank()) {
            return patientName;
        }
        return patientName + "（" + patientGender + "）";
    }

    /** 流水号展示，如 APT000012 */
    public String getSerialNo() {
        return id == null ? "-" : String.format("APT%06d", id);
    }
}
