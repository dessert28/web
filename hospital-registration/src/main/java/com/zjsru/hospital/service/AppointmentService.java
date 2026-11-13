package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Appointment;
import com.zjsru.hospital.domain.BookingResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 挂号业务：提交、查询、取消、医生端按日期筛选。
 */
public interface AppointmentService {

    /** 可预约的时段白名单，模板直接遍历渲染，避免前后端两套枚举 */
    List<String> queryTimeSlots();

    /** 最早可预约日期（今天） */
    LocalDate earliestDate();

    /** 最晚可预约日期（今天 + 14 天） */
    LocalDate latestDate();

    /**
     * 提交挂号。
     *
     * @param appointment 已填充 doctorId / userId / 就诊人信息 / visitDate / timeSlot
     * @return 成功带 appointment（id 已回填），失败带原因
     */
    BookingResult submit(Appointment appointment);

    /** 结果页/取消页取单条，仅本人可见；不存在或不属于该用户返回 null */
    Appointment queryByIdForUser(Integer id, Integer userId);

    /** 患者的预约列表 */
    List<Appointment> queryByUserId(Integer userId);

    /**
     * 取消预约。
     *
     * @return true 表示本次确实取消成功；false 表示不存在 / 不属于该用户 / 已取消
     */
    boolean cancel(Integer id, Integer userId);

    /** 医生按日期查询（status 为 null 表示不限） */
    List<Appointment> queryByDoctorAndDate(Integer doctorId, LocalDate visitDate, Integer status);

    /** 医生按日期区间查询 */
    List<Appointment> queryByDoctorAndDateRange(Integer doctorId, LocalDate from, LocalDate to, Integer status);
}
