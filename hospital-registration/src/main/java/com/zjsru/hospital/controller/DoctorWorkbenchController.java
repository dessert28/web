package com.zjsru.hospital.controller;

import com.zjsru.hospital.domain.Appointment;
import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.interceptor.LoginInterceptor;
import com.zjsru.hospital.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生端工作台：查看本人就诊预约，支持按日期/状态筛选。
 * <p>
 * 医生身份只从会话取，不接受外部 doctorId 参数，无法越权查看他人预约。
 */
@Controller
public class DoctorWorkbenchController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/doctor/appointments")
    public ModelAndView appointments(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "range", required = false) String range,
            HttpSession session) {

        LoginUser loginUser = (LoginUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        Integer doctorId = loginUser.getDoctorId();

        ModelAndView mv = new ModelAndView("doctorAppointments");
        LocalDate today = LocalDate.now();

        // 日期解析：非法输入回退今天并给出提示，不抛 500
        LocalDate visitDate = today;
        String message = null;
        if (date != null && !date.isBlank()) {
            try {
                visitDate = LocalDate.parse(date.trim());
            } catch (RuntimeException ex) {
                message = "日期格式不正确，已显示今天的预约";
                visitDate = today;
            }
        }

        // 状态筛选：空=全部，0=待就诊，1=已取消
        Integer statusFilter = null;
        if ("0".equals(status) || "1".equals(status)) {
            statusFilter = Integer.valueOf(status);
        }

        List<Appointment> appointments;
        if ("week".equals(range)) {
            // 时间筛选：未来 7 天区间
            appointments = appointmentService.queryByDoctorAndDateRange(
                    doctorId, today, today.plusDays(6), statusFilter);
        } else {
            appointments = appointmentService.queryByDoctorAndDate(doctorId, visitDate, statusFilter);
        }

        long pendingCount = appointments.stream().filter(a -> !a.isCancelled()).count();

        mv.addObject("loginUser", loginUser);
        mv.addObject("appointments", appointments);
        mv.addObject("today", today);
        mv.addObject("visitDate", visitDate);
        mv.addObject("dateParam", date);
        mv.addObject("status", statusFilter);
        mv.addObject("range", range);
        mv.addObject("pendingCount", pendingCount);
        mv.addObject("isToday", visitDate.equals(today));
        mv.addObject("message", message);
        return mv;
    }

    /** 今日预约的便捷入口 */
    @GetMapping("/doctor/appointments/today")
    public ModelAndView todayAppointments() {
        return new ModelAndView("redirect:/doctor/appointments");
    }

    /** 兼容一个显式的日期查询入口（与上面同一个视图） */
    @GetMapping("/doctor/appointments/query")
    public ModelAndView queryByDate(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String target = date == null ? "" : "?date=" + date;
        return new ModelAndView("redirect:/doctor/appointments" + target);
    }
}
