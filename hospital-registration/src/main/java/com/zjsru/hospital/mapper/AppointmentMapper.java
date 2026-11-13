package com.zjsru.hospital.mapper;

import com.zjsru.hospital.domain.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约挂号数据访问（tb_appointment）。
 * XML 在 resources/mapper/AppointmentMapper.xml。
 */
@Mapper
public interface AppointmentMapper {

    // 新增预约，插入后 appointment.id 会被回填
    int insertAppointment(Appointment appointment);

    // 按主键查询（JOIN 医生与科室，供结果页展示）
    Appointment queryById(@Param("id") Integer id);

    // 按主键 + 归属用户查询（防止看别人的预约）
    Appointment queryByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    // 患者自己的预约列表（按就诊日期倒序）
    List<Appointment> queryByUserId(@Param("userId") Integer userId);

    // 同医生 + 同日 + 同时段是否已有待就诊预约（重复预约检查）
    int countActiveBySlot(@Param("doctorId") Integer doctorId,
                          @Param("visitDate") LocalDate visitDate,
                          @Param("timeSlot") String timeSlot);

    // 取消预约：附带 user_id 与 status 条件，用受影响行数判断越权/重复
    int cancelByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    // 医生按日期查预约（status 为空表示不限状态）
    List<Appointment> queryByDoctorAndDate(@Param("doctorId") Integer doctorId,
                                           @Param("visitDate") LocalDate visitDate,
                                           @Param("status") Integer status);

    // 医生按日期区间查预约（时间筛选）
    List<Appointment> queryByDoctorAndDateRange(@Param("doctorId") Integer doctorId,
                                                @Param("from") LocalDate from,
                                                @Param("to") LocalDate to,
                                                @Param("status") Integer status);
}
