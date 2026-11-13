package com.zjsru.hospital;

import com.zjsru.hospital.interceptor.LoginInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 端到端路由测试：登录拦截、页面渲染、挂号/取消、医生端权限隔离。
 * 真实连 MySQL，用例自清理自己造的数据。
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationFlowTests {

    private static final String TEST_PATIENT = "流程测试就诊人";
    private static final int PATIENT_USER_ID = 2;   // tb_users: 15998815467
    private static final int DOCTOR_ID = 1;         // doctor_info: 张伟

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM tb_appointment WHERE patient_name = ?", TEST_PATIENT);
    }

    // ------------------------------------------------------------ 匿名可访问

    @Test
    @DisplayName("首页无需登录：200 且列出 6 个科室")
    void homeIsPublic() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientHome"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("心内科")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("皮肤科")));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientHome"));
    }

    @Test
    @DisplayName("检索无需登录：命中/未命中都 200")
    void searchIsPublic() throws Exception {
        mockMvc.perform(get("/search").param("keyword", "儿"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("儿科")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("皮肤科"))));

        mockMvc.perform(get("/search").param("keyword", "不存在的科室XYZ"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("没有找到匹配")));
    }

    // ------------------------------------------------------------ 登录拦截

    @Test
    @DisplayName("未登录访问科室页 → 302 到 /toLogin?redirect=")
    void departmentRequiresLogin() throws Exception {
        mockMvc.perform(get("/department/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/toLogin?redirect=*"));

        mockMvc.perform(get("/doctorDetail/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/toLogin?redirect=*"));

        mockMvc.perform(get("/myAppointments"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/toLogin?redirect=*"));
    }

    @Test
    @DisplayName("登录后科室页 200 并展示医生；未知科室 200 不 500")
    void departmentDetailAfterLogin() throws Exception {
        MockHttpSession session = patientSession();

        mockMvc.perform(get("/department/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("departmentDetail"))
                .andExpect(model().attributeExists("department"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("心内科")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("张伟")));

        mockMvc.perform(get("/department/9999").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("不存在或已下线")));
    }

    @Test
    @DisplayName("医生详情页展示姓名/职称/专业专长")
    void doctorDetailPage() throws Exception {
        mockMvc.perform(get("/doctorDetail/1").session(patientSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("doctorDetail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("张伟")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("主任医师")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("专业专长")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("冠心病")));
    }

    // ------------------------------------------------------------ 挂号

    @Test
    @DisplayName("提交挂号 → 303 到结果页，库里多一行")
    void submitAppointmentFlow() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        int before = countRows();

        MvcResult result = mockMvc.perform(post("/appointment/submit").session(patientSession())
                        .param("doctorId", String.valueOf(DOCTOR_ID))
                        .param("visitDate", date.toString())
                        .param("timeSlot", "上午 09:00-10:00")
                        .param("patientName", TEST_PATIENT)
                        .param("patientGender", "男")
                        .param("patientPhone", "13900000000")
                        .param("patientIdCard", "330106199001010011")
                        .param("remark", "流程测试"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        assertThat(location).startsWith("/appointment/result/");
        assertThat(countRows()).isEqualTo(before + 1);

        mockMvc.perform(get(location).session(patientSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("appointmentResult"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("预约成功")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(TEST_PATIENT)));
    }

    @Test
    @DisplayName("重复挂号被拦下：仍是 200 表单页 + 错误提示，库里不新增")
    void duplicateAppointmentBlocked() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        String slot = "上午 08:00-09:00";
        submitOnce(date, slot);
        int afterFirst = countRows();

        mockMvc.perform(post("/appointment/submit").session(patientSession())
                        .param("doctorId", String.valueOf(DOCTOR_ID))
                        .param("visitDate", date.toString())
                        .param("timeSlot", slot)
                        .param("patientName", TEST_PATIENT)
                        .param("patientPhone", "13900000000"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctorDetail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("已被预约")));

        assertThat(countRows()).isEqualTo(afterFirst);
    }

    @Test
    @DisplayName("过期日期被拦下：库里不新增")
    void pastDateBlocked() throws Exception {
        int before = countRows();

        mockMvc.perform(post("/appointment/submit").session(patientSession())
                        .param("doctorId", String.valueOf(DOCTOR_ID))
                        .param("visitDate", LocalDate.now().minusDays(1).toString())
                        .param("timeSlot", "上午 09:00-10:00")
                        .param("patientName", TEST_PATIENT)
                        .param("patientPhone", "13900000000"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("不能早于今天")));

        assertThat(countRows()).isEqualTo(before);
    }

    @Test
    @DisplayName("取消预约：状态变已取消，重复取消不报错")
    void cancelAppointmentFlow() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        String location = submitOnce(date, "下午 13:30-14:30");
        Integer id = Integer.valueOf(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(post("/appointment/cancel").session(patientSession())
                        .param("id", String.valueOf(id))
                        .param("from", "result"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("预约已取消")));

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM tb_appointment WHERE id = ?", Integer.class, id);
        assertThat(status).isEqualTo(1);

        // 再取消一次：幂等，不抛异常
        mockMvc.perform(post("/appointment/cancel").session(patientSession())
                        .param("id", String.valueOf(id))
                        .param("from", "result"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取消别人的预约无效")
    void cancelOthersAppointmentNoEffect() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        String location = submitOnce(date, "下午 14:30-15:30");
        Integer id = Integer.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // 用另一个患者会话（tb_users id=20）去取消
        mockMvc.perform(post("/appointment/cancel").session(patientSession("shitou", "123"))
                        .param("id", String.valueOf(id))
                        .param("from", "result"))
                .andExpect(status().isOk());

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM tb_appointment WHERE id = ?", Integer.class, id);
        assertThat(status).isEqualTo(0);
    }

    @Test
    @DisplayName("我的预约列表展示自己的记录")
    void myAppointmentsPage() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        submitOnce(date, "下午 15:30-16:30");

        mockMvc.perform(get("/myAppointments").session(patientSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("myAppointments"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(TEST_PATIENT)));
    }

    // ------------------------------------------------------------ 医生端

    @Test
    @DisplayName("医生端默认显示今天；patientDate 参数下只看到自己那天的预约")
    void doctorWorkbench() throws Exception {
        LocalDate today = LocalDate.now();
        jdbcTemplate.update("INSERT INTO tb_appointment(doctor_id, user_id, patient_name, patient_gender,"
                        + " patient_phone, visit_date, time_slot, status) VALUES (?,?,?,?,?,?,?,0)",
                DOCTOR_ID, PATIENT_USER_ID, TEST_PATIENT, "男", "13900000000", today, "上午 11:00-12:00");

        mockMvc.perform(get("/doctor/appointments").session(doctorSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("doctorAppointments"))
                .andExpect(model().attribute("isToday", true))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(TEST_PATIENT)));
    }

    @Test
    @DisplayName("医生端按日期筛选：昨天没有数据时列表为空")
    void doctorWorkbenchFiltersByDate() throws Exception {
        LocalDate today = LocalDate.now();
        jdbcTemplate.update("INSERT INTO tb_appointment(doctor_id, user_id, patient_name, patient_gender,"
                        + " patient_phone, visit_date, time_slot, status) VALUES (?,?,?,?,?,?,?,0)",
                DOCTOR_ID, PATIENT_USER_ID, TEST_PATIENT, "男", "13900000000", today, "下午 16:30-17:30");

        mockMvc.perform(get("/doctor/appointments").session(doctorSession())
                        .param("date", today.minusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isToday", false))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("该条件下没有预约记录")));
    }

    @Test
    @DisplayName("医生端不显示其他医生的预约")
    void doctorWorkbenchIsolated() throws Exception {
        LocalDate today = LocalDate.now();
        // 李娜（id 2）名下的预约
        jdbcTemplate.update("INSERT INTO tb_appointment(doctor_id, user_id, patient_name, patient_gender,"
                        + " patient_phone, visit_date, time_slot, status) VALUES (?,?,?,?,?,?,?,0)",
                2, PATIENT_USER_ID, TEST_PATIENT, "男", "13900000000", today, "上午 08:00-09:00");

        mockMvc.perform(get("/doctor/appointments").session(doctorSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(TEST_PATIENT))));
    }

    @Test
    @DisplayName("患者访问医生端 → 403")
    void patientCannotReachDoctorArea() throws Exception {
        mockMvc.perform(get("/doctor/appointments").session(patientSession()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/doctor/appointments/today").session(patientSession()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未登录访问医生端 → 302 到登录页")
    void anonymousCannotReachDoctorArea() throws Exception {
        mockMvc.perform(get("/doctor/appointments"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/toLogin?redirect=*"));
    }

    // ------------------------------------------------------------ 登录/退出

    @Test
    @DisplayName("患者登录成功 → 重定向；失败 → 回到登录页带错误提示")
    void loginPageFlow() throws Exception {
        MvcResult ok = mockMvc.perform(post("/login")
                        .param("username", "15998815467")
                        .param("password", "123")
                        .param("loginType", "patient"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(ok.getResponse().getRedirectedUrl()).isEqualTo("/home");

        mockMvc.perform(post("/login")
                        .param("username", "15998815467")
                        .param("password", "wrong")
                        .param("loginType", "patient"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("用户名或密码错误")));

        mockMvc.perform(post("/login")
                        .param("username", "张伟")
                        .param("password", "123")
                        .param("loginType", "doctor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/doctor/appointments"));
    }

    @Test
    @DisplayName("退出登录清空会话，受保护页面重新要求登录")
    void logoutClearsSession() throws Exception {
        MockHttpSession session = patientSession();
        assertThat(session.getAttribute(LoginInterceptor.SESSION_USER)).isNotNull();

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/home"));

        // invalidate 之后同一个 session 对象不可再读，换一个空会话验证受保护页面重新要登录
        assertThat(session.isInvalid()).isTrue();
        mockMvc.perform(get("/myAppointments").session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/toLogin?redirect=*"));
    }

    // ------------------------------------------------------------ 工具

    private MockHttpSession patientSession() throws Exception {
        return patientSession("15998815467", "123");
    }

    private MockHttpSession patientSession(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password)
                        .param("loginType", "patient"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private MockHttpSession doctorSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", "张伟")
                        .param("password", "123")
                        .param("loginType", "doctor"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String submitOnce(LocalDate date, String slot) throws Exception {
        MvcResult result = mockMvc.perform(post("/appointment/submit").session(patientSession())
                        .param("doctorId", String.valueOf(DOCTOR_ID))
                        .param("visitDate", date.toString())
                        .param("timeSlot", slot)
                        .param("patientName", TEST_PATIENT)
                        .param("patientGender", "男")
                        .param("patientPhone", "13900000000"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return result.getResponse().getRedirectedUrl();
    }

    private int countRows() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_appointment WHERE patient_name = ?", Integer.class, TEST_PATIENT);
        return count == null ? 0 : count;
    }
}
