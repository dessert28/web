package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Appointment;
import com.zjsru.hospital.domain.BookingResult;
import com.zjsru.hospital.domain.Doctor;
import com.zjsru.hospital.mapper.AppointmentMapper;
import com.zjsru.hospital.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 挂号业务实现。
 * <p>
 * 重复预约不做数据库级唯一约束（取消后要允许重新预约，唯一索引会误杀），
 * 改在服务层按「同医生 + 同日 + 同时段 + status=0」查重。
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    /** 可预约时段白名单 */
    public static final List<String> TIME_SLOTS = List.of(
            "上午 08:00-09:00",
            "上午 09:00-10:00",
            "上午 10:00-11:00",
            "上午 11:00-12:00",
            "下午 13:30-14:30",
            "下午 14:30-15:30",
            "下午 15:30-16:30",
            "下午 16:30-17:30"
    );

    /** 最多可提前预约的天数 */
    public static final int MAX_ADVANCE_DAYS = 14;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<String> queryTimeSlots() {
        return TIME_SLOTS;
    }

    @Override
    public LocalDate earliestDate() {
        return LocalDate.now();
    }

    @Override
    public LocalDate latestDate() {
        return LocalDate.now().plusDays(MAX_ADVANCE_DAYS);
    }

    @Override
    public BookingResult submit(Appointment appointment) {
        if (appointment == null) {
            return BookingResult.fail(BookingResult.Code.MISSING_FIELD, "预约信息为空");
        }
        if (appointment.getDoctorId() == null) {
            return BookingResult.fail(BookingResult.Code.NO_DOCTOR, "请选择医生");
        }
        if (appointment.getUserId() == null) {
            return BookingResult.fail(BookingResult.Code.MISSING_FIELD, "登录状态已失效，请重新登录");
        }
        if (isBlank(appointment.getPatientName())) {
            return BookingResult.fail(BookingResult.Code.MISSING_FIELD, "请填写就诊人姓名");
        }
        if (isBlank(appointment.getPatientPhone())) {
            return BookingResult.fail(BookingResult.Code.MISSING_FIELD, "请填写联系电话");
        }

        Doctor doctor = doctorMapper.queryDoctorById(appointment.getDoctorId());
        if (doctor == null) {
            return BookingResult.fail(BookingResult.Code.NO_DOCTOR, "医生不存在，请重新选择");
        }

        LocalDate visitDate = appointment.getVisitDate();
        if (visitDate == null) {
            return BookingResult.fail(BookingResult.Code.BAD_DATE, "请选择就诊日期");
        }
        if (visitDate.isBefore(earliestDate())) {
            return BookingResult.fail(BookingResult.Code.BAD_DATE, "就诊日期不能早于今天");
        }
        if (visitDate.isAfter(latestDate())) {
            return BookingResult.fail(BookingResult.Code.BAD_DATE,
                    "最多只能提前 " + MAX_ADVANCE_DAYS + " 天预约");
        }

        if (isBlank(appointment.getTimeSlot()) || !TIME_SLOTS.contains(appointment.getTimeSlot())) {
            return BookingResult.fail(BookingResult.Code.BAD_SLOT, "请选择有效的就诊时段");
        }

        int active = appointmentMapper.countActiveBySlot(
                appointment.getDoctorId(), visitDate, appointment.getTimeSlot());
        if (active > 0) {
            return BookingResult.fail(BookingResult.Code.DUPLICATE,
                    "该医生在 " + visitDate + " " + appointment.getTimeSlot() + " 已被预约，请选择其他时段");
        }

        appointment.setStatus(0);
        appointmentMapper.insertAppointment(appointment);
        // 回填展示字段，结果页直接可用
        appointment.setDoctorName(doctor.getDoctorName());
        appointment.setDoctorTitle(doctor.getTitle());
        return BookingResult.ok(appointment);
    }

    @Override
    public Appointment queryByIdForUser(Integer id, Integer userId) {
        if (id == null || userId == null) {
            return null;
        }
        return appointmentMapper.queryByIdAndUserId(id, userId);
    }

    @Override
    public List<Appointment> queryByUserId(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return appointmentMapper.queryByUserId(userId);
    }

    @Override
    public boolean cancel(Integer id, Integer userId) {
        if (id == null || userId == null) {
            return false;
        }
        // 用受影响行数判断：别人的预约、已取消的预约都会返回 0
        return appointmentMapper.cancelByIdAndUserId(id, userId) > 0;
    }

    @Override
    public List<Appointment> queryByDoctorAndDate(Integer doctorId, LocalDate visitDate, Integer status) {
        if (doctorId == null || visitDate == null) {
            return List.of();
        }
        return appointmentMapper.queryByDoctorAndDate(doctorId, visitDate, status);
    }

    @Override
    public List<Appointment> queryByDoctorAndDateRange(Integer doctorId, LocalDate from, LocalDate to,
                                                       Integer status) {
        if (doctorId == null || from == null || to == null) {
            return List.of();
        }
        return appointmentMapper.queryByDoctorAndDateRange(doctorId, from, to, status);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
