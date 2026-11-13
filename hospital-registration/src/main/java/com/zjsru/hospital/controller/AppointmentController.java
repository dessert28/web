package com.zjsru.hospital.controller;

import com.zjsru.hospital.domain.Appointment;
import com.zjsru.hospital.domain.BookingResult;
import com.zjsru.hospital.domain.Doctor;
import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.interceptor.LoginInterceptor;
import com.zjsru.hospital.service.AppointmentService;
import com.zjsru.hospital.service.DepartmentService;
import com.zjsru.hospital.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;

/**
 * 医生详情页与挂号流程（全部需登录，由 LoginInterceptor 保证）。
 */
@Controller
public class AppointmentController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AppointmentService appointmentService;

    /** 医生详情 + 挂号表单 */
    @GetMapping("/doctorDetail/{doctorId}")
    public ModelAndView doctorDetail(@PathVariable("doctorId") Integer doctorId,
                                     HttpSession session) {
        ModelAndView mv = new ModelAndView("doctorDetail");
        LoginUser loginUser = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        Doctor doctor = doctorService.queryDoctorById(doctorId);

        mv.addObject("doctor", doctor);
        mv.addObject("doctorId", doctorId);
        mv.addObject("loginUser", loginUser);
        mv.addObject("timeSlots", appointmentService.queryTimeSlots());
        mv.addObject("earliestDate", appointmentService.earliestDate());
        mv.addObject("latestDate", appointmentService.latestDate());
        if (doctor == null) {
            mv.addObject("message", "医生不存在或已下线");
        } else if (doctor.getDepartmentId() != null) {
            mv.addObject("department", departmentService.queryDepartmentById(doctor.getDepartmentId()));
        }
        return mv;
    }

    /** 提交挂号 */
    @PostMapping("/appointment/submit")
    public ModelAndView submit(@RequestParam(value = "doctorId", required = false) Integer doctorId,
                               @RequestParam(value = "visitDate", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
                               @RequestParam(value = "timeSlot", required = false) String timeSlot,
                               @RequestParam(value = "patientName", required = false) String patientName,
                               @RequestParam(value = "patientGender", required = false) String patientGender,
                               @RequestParam(value = "patientPhone", required = false) String patientPhone,
                               @RequestParam(value = "patientIdCard", required = false) String patientIdCard,
                               @RequestParam(value = "remark", required = false) String remark,
                               HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_USER);

        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctorId);
        appointment.setUserId(loginUser.getAccountId());
        appointment.setVisitDate(visitDate);
        appointment.setTimeSlot(timeSlot);
        appointment.setPatientName(trim(patientName));
        appointment.setPatientGender(trim(patientGender));
        appointment.setPatientPhone(trim(patientPhone));
        appointment.setPatientIdCard(trim(patientIdCard));
        appointment.setRemark(trim(remark));

        BookingResult result = appointmentService.submit(appointment);
        if (result.isOk()) {
            return new ModelAndView("redirect:/appointment/result/" + result.getAppointment().getId());
        }

        // 失败：回填表单，保留用户已填内容
        ModelAndView mv = new ModelAndView("doctorDetail");
        Doctor doctor = doctorId == null ? null : doctorService.queryDoctorById(doctorId);
        mv.addObject("doctor", doctor);
        mv.addObject("doctorId", doctorId);
        mv.addObject("loginUser", loginUser);
        mv.addObject("timeSlots", appointmentService.queryTimeSlots());
        mv.addObject("earliestDate", appointmentService.earliestDate());
        mv.addObject("latestDate", appointmentService.latestDate());
        mv.addObject("form", appointment);
        mv.addObject("message", result.getMessage());
        if (doctor != null && doctor.getDepartmentId() != null) {
            mv.addObject("department", departmentService.queryDepartmentById(doctor.getDepartmentId()));
        }
        return mv;
    }

    /** 挂号结果（仅本人可看） */
    @GetMapping("/appointment/result/{id}")
    public ModelAndView result(@PathVariable("id") Integer id, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        ModelAndView mv = new ModelAndView("appointmentResult");
        Appointment appointment = appointmentService.queryByIdForUser(id, loginUser.getAccountId());
        mv.addObject("loginUser", loginUser);
        mv.addObject("appointment", appointment);
        if (appointment == null) {
            mv.addObject("message", "预约不存在，或不属于当前账号");
        }
        return mv;
    }

    /** 我的预约 */
    @GetMapping("/myAppointments")
    public ModelAndView myAppointments(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        ModelAndView mv = new ModelAndView("myAppointments");
        mv.addObject("loginUser", loginUser);
        mv.addObject("appointments", appointmentService.queryByUserId(loginUser.getAccountId()));
        return mv;
    }

    /** 取消预约（POST，仅本人、仅待就诊） */
    @PostMapping("/appointment/cancel")
    public ModelAndView cancel(@RequestParam("id") Integer id,
                               @RequestParam(value = "from", required = false) String from,
                               HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        boolean cancelled = appointmentService.cancel(id, loginUser.getAccountId());

        if ("result".equals(from)) {
            ModelAndView mv = new ModelAndView("appointmentResult");
            Appointment appointment = appointmentService.queryByIdForUser(id, loginUser.getAccountId());
            mv.addObject("loginUser", loginUser);
            mv.addObject("appointment", appointment);
            mv.addObject("message", cancelled ? "预约已取消" : "预约不存在或已取消，无需重复操作");
            mv.addObject("cancelDone", cancelled);
            return mv;
        }

        ModelAndView mv = new ModelAndView("redirect:/myAppointments");
        if (!cancelled) {
            mv.addObject("message", "预约不存在或已取消");
        }
        return mv;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
