package com.zjsru.domain;

import lombok.Data;

/** 医生实体：对应 mybatis 库的 doctor_info 表；Lombok 生成访问器。
    注意：表中 gender 是 tinyint（0-女，1-男，2-其他），这里声明为 String，
    读写时会按字符串转换，所以传值必须是 "0"/"1"/"2"，传"男""女"会插入失败。 */
@Data
public class Doctor {
    private Integer id;
    // 列名本身就是 doctorName（varchar），与字段同名，无需 @Results 映射
    private String doctorName;
    private String gender;
    private String department;
    private String title;
    private String phone;
}
