package com.zjsru.hospital.domain;

import lombok.Data;

/**
 * 医生实体，对应 doctor_info 表。
 * specialty（专业专长）与 password（登录密码）由 db/experiment2-appointment.sql 追加。
 */
@Data
public class Doctor {
    private Integer id;
    private String doctorName;
    // 数据库里是 tinyint（0-女，1-男，2-其他），这里用 String 承接，由模板/控制器做展示转换
    private String gender;
    // 历史文本科室列，与 departmentId 并存
    private String department;
    private String title;
    private String phone;
    // 专业专长（实验二新增列）
    private String specialty;
    // 所属科室ID，关联 doctor_department.id
    private Integer departmentId;
    // 所属科室对象（容器内嵌 doctor 时使用）
    private Department dept;

    /**
     * 性别展示值：把 tinyint 的 0/1/2 转成中文。
     */
    public String getGenderLabel() {
        if (gender == null) {
            return "-";
        }
        String value = gender.trim();
        if ("1".equals(value)) {
            return "男";
        }
        if ("0".equals(value)) {
            return "女";
        }
        if ("2".equals(value)) {
            return "其他";
        }
        return value;
    }
}
