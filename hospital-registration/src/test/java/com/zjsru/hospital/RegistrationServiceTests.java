package com.zjsru.hospital;

import com.zjsru.hospital.domain.Appointment;
import com.zjsru.hospital.domain.BookingResult;
import com.zjsru.hospital.domain.Department;
import com.zjsru.hospital.domain.Doctor;
import com.zjsru.hospital.domain.LoginUser;
import com.zjsru.hospital.domain.Role;
import com.zjsru.hospital.domain.UserCredential;
import com.zjsru.hospital.service.AppointmentService;
import com.zjsru.hospital.service.AuthService;
import com.zjsru.hospital.service.DepartmentService;
import com.zjsru.hospital.service.DoctorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 业务层测试：科室一对多映射、登录查库、挂号校验/查重/取消、医生端隔离。
 * 依赖真实 MySQL（mybatis 库），用例自清理 tb_appointment 中自己造的数据。
 */
@SpringBootTest
class RegistrationServiceTests {

    /** 儿科：种子数据里医生最多（3 名：id 2/9/10） */
    private static final int DEPT_ID_WITH_MANY_DOCTORS = 2;
    private static final int SEED_DEPT_DOCTOR_COUNT = 3;

    /** 测试专用就诊人姓名，用于精确清理自己插的行 */
    private static final String TEST_PATIENT = "单元测试就诊人";

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM tb_appointment WHERE patient_name = ?", TEST_PATIENT);
    }

    // ---------------------------------------------------------------- 科室

    @Test
    @DisplayName("首页科室列表：6 个科室且字段非空（camelCase 别名回归）")
    void queryAllDepartmentsReturnsSeededDepartments() {
        List<Department> departments = departmentService.queryAllDepartments();

        assertThat(departments).hasSize(6);
        assertThat(departments).allSatisfy(dept -> {
            assertThat(dept.getId()).isNotNull();
            assertThat(dept.getDeptName()).isNotBlank();
            assertThat(dept.getLocation()).isNotBlank();
            assertThat(dept.getDeptPhone()).isNotBlank();
        });
        assertThat(departments.get(0).getDeptName()).isEqualTo("心内科");
    }

    @Test
    @DisplayName("科室详情：一对多映射出该科室全部医生")
    void queryDepartmentWithDoctors() {
        Department department = departmentService.queryDepartmentInfoAndDoctorsByDeptId(DEPT_ID_WITH_MANY_DOCTORS);

        assertThat(department).isNotNull();
        assertThat(department.getDeptName()).isEqualTo("儿科");
        assertThat(department.getDoctorList()).hasSize(SEED_DEPT_DOCTOR_COUNT);
        assertThat(department.getDoctorList()).allSatisfy(doctor -> {
            assertThat(doctor.getId()).isNotNull();
            assertThat(doctor.getDoctorName()).isNotBlank();
            assertThat(doctor.getDepartmentId()).isEqualTo(DEPT_ID_WITH_MANY_DOCTORS);
        });
    }

    @Test
    @DisplayName("未知科室返回 null，而不是抛异常")
    void queryUnknownDepartmentReturnsNull() {
        assertThat(departmentService.queryDepartmentInfoAndDoctorsByDeptId(9999)).isNull();
        assertThat(departmentService.queryDepartmentInfoAndDoctorsByDeptId(null)).isNull();
    }

    @Test
    @DisplayName("科室检索：按科室名命中")
    void searchByDepartmentName() {
        var results = departmentService.search("儿科");

        assertThat(results).isNotEmpty();
        assertThat(results).anySatisfy(result ->
                assertThat(result.getDepartment().getDeptName()).isEqualTo("儿科"));
    }

    @Test
    @DisplayName("科室检索：只命中医生时也把所属科室带出来")
    void searchByDoctorNameBringsDepartment() {
        var results = departmentService.search("王启明");

        assertThat(results).isNotEmpty();
        assertThat(results).anySatisfy(result -> {
            assertThat(result.isMatchedByDoctor()).isTrue();
            assertThat(result.getMatchedDoctors())
                    .allSatisfy(doctor -> assertThat(doctor.getDoctorName()).isEqualTo("王启明"));
        });
    }

    @Test
    @DisplayName("科室检索：无关键字返回全部科室，无匹配返回空")
    void searchEdgeCases() {
        assertThat(departmentService.search("")).hasSize(6);
        assertThat(departmentService.search(null)).hasSize(6);
        assertThat(departmentService.search("不存在的科室XYZ")).isEmpty();
    }

    // ---------------------------------------------------------------- 医生

    @Test
    @DisplayName("医生详情带出专业专长（实验二新增列）")
    void doctorDetailHasSpecialty() {
        Doctor doctor = doctorService.queryDoctorById(1);

        assertThat(doctor).isNotNull();
        assertThat(doctor.getDoctorName()).isEqualTo("张伟");
        assertThat(doctor.getTitle()).isEqualTo("主任医师");
        assertThat(doctor.getSpecialty()).isNotBlank();
        assertThat(doctor.getGenderLabel()).isEqualTo("男");
    }

    @Test
    @DisplayName("医生 id 为空/不存在时返回 null，不抛异常")
    void doctorDetailEdgeCases() {
        assertThat(doctorService.queryDoctorById(null)).isNull();
        assertThat(doctorService.queryDoctorById(9999)).isNull();
    }

    // ---------------------------------------------------------------- 登录

    @Test
    @DisplayName("患者登录：查 tb_users 成功")
    void patientLoginHitsDatabase() {
        LoginUser user = authService.login(credential("15998815467", "123", "patient"));

        assertThat(user).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.PATIENT);
        assertThat(user.getRole().getLabel()).isEqualTo("患者");
        assertThat(user.getAccountId()).isNotNull();
        assertThat(user.getDoctorId()).isNull();
    }

    @Test
    @DisplayName("医生登录：查 doctor_info 成功，角色为医生")
    void doctorLoginHitsDatabase() {
        LoginUser user = authService.login(credential("张伟", "123", "doctor"));

        assertThat(user).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.DOCTOR);
        assertThat(user.getRole().getLabel()).isEqualTo("医生");
        assertThat(user.getDoctorId()).isEqualTo(1);
    }

    @Test
    @DisplayName("密码错误 / 角色错配 / 空提交都返回 null")
    void loginFailures() {
        assertThat(authService.login(credential("15998815467", "wrong", "patient"))).isNull();
        assertThat(authService.login(credential("张伟", "wrong", "doctor"))).isNull();
        // 患者账号名拿到医生表里查，查不到
        assertThat(authService.login(credential("15998815467", "123", "doctor"))).isNull();
        // 医生姓名拿到 tb_users 里查，也查不到
        assertThat(authService.login(credential("张伟", "123", "patient"))).isNull();
        assertThat(authService.login(credential("", "", "patient"))).isNull();
        assertThat(authService.login(null)).isNull();
    }

    // ---------------------------------------------------------------- 挂号

    @Test
    @DisplayName("提交挂号成功：返回 id，库里多一行")
    void submitAppointmentSucceeds() {
        LocalDate date = LocalDate.now().plusDays(3);
        int before = countAppointments();

        BookingResult result = appointmentService.submit(
                booking(1, 2, date, "上午 09:00-10:00", TEST_PATIENT));

        assertThat(result.isOk()).isTrue();
        assertThat(result.getAppointment().getId()).isNotNull();
        assertThat(result.getAppointment().getSerialNo()).startsWith("APT");
        assertThat(countAppointments()).isEqualTo(before + 1);

        // 结果页需要的展示字段已回填（科室名走 JOIN）
        Appointment saved = appointmentService.queryByIdForUser(result.getAppointment().getId(), 2);
        assertThat(saved).isNotNull();
        assertThat(saved.getDoctorName()).isEqualTo("张伟");
        assertThat(saved.getDeptName()).isEqualTo("心内科");
        assertThat(saved.isCancelled()).isFalse();
    }

    @Test
    @DisplayName("同医生 + 同日 + 同时段重复预约被拒绝")
    void duplicateAppointmentRejected() {
        LocalDate date = LocalDate.now().plusDays(4);
        BookingResult first = appointmentService.submit(
                booking(1, 2, date, "下午 14:30-15:30", TEST_PATIENT));
        assertThat(first.isOk()).isTrue();

        int afterFirst = countAppointments();
        BookingResult second = appointmentService.submit(
                booking(1, 2, date, "下午 14:30-15:30", TEST_PATIENT));

        assertThat(second.isOk()).isFalse();
        assertThat(second.getCode()).isEqualTo(BookingResult.Code.DUPLICATE);
        assertThat(countAppointments()).isEqualTo(afterFirst);
    }

    @Test
    @DisplayName("过去日期 / 超出提前天数 / 非法时段 / 缺字段都被拒绝")
    void invalidBookingsRejected() {
        int before = countAppointments();

        assertThat(appointmentService.submit(
                booking(1, 2, LocalDate.now().minusDays(1), "上午 09:00-10:00", TEST_PATIENT)).getCode())
                .isEqualTo(BookingResult.Code.BAD_DATE);
        assertThat(appointmentService.submit(
                booking(1, 2, LocalDate.now().plusDays(AppointmentServiceImplLimits.MAX_ADVANCE_DAYS + 1),
                        "上午 09:00-10:00", TEST_PATIENT)).getCode())
                .isEqualTo(BookingResult.Code.BAD_DATE);
        assertThat(appointmentService.submit(
                booking(1, 2, LocalDate.now().plusDays(1), "凌晨 03:00-04:00", TEST_PATIENT)).getCode())
                .isEqualTo(BookingResult.Code.BAD_SLOT);
        assertThat(appointmentService.submit(
                booking(9999, 2, LocalDate.now().plusDays(1), "上午 09:00-10:00", TEST_PATIENT)).getCode())
                .isEqualTo(BookingResult.Code.NO_DOCTOR);
        assertThat(appointmentService.submit(
                booking(1, 2, LocalDate.now().plusDays(1), "上午 09:00-10:00", "   ")).getCode())
                .isEqualTo(BookingResult.Code.MISSING_FIELD);

        assertThat(countAppointments()).isEqualTo(before);
    }

    @Test
    @DisplayName("取消自己的预约成功，二次取消无副作用（幂等）")
    void cancelIsIdempotent() {
        BookingResult result = appointmentService.submit(
                booking(1, 2, LocalDate.now().plusDays(5), "上午 10:00-11:00", TEST_PATIENT));
        Integer id = result.getAppointment().getId();

        assertThat(appointmentService.cancel(id, 2)).isTrue();
        assertThat(appointmentService.queryByIdForUser(id, 2).isCancelled()).isTrue();

        assertThat(appointmentService.cancel(id, 2)).isFalse();
        assertThat(appointmentService.queryByIdForUser(id, 2).isCancelled()).isTrue();
    }

    @Test
    @DisplayName("取消别人的预约无效，看别人的预约也查不到")
    void crossUserAccessBlocked() {
        BookingResult result = appointmentService.submit(
                booking(1, 2, LocalDate.now().plusDays(6), "上午 11:00-12:00", TEST_PATIENT));
        Integer id = result.getAppointment().getId();

        // 用户 1 试图取消用户 2 的预约
        assertThat(appointmentService.cancel(id, 1)).isFalse();
        assertThat(appointmentService.queryByIdForUser(id, 2).isCancelled()).isFalse();
        // 用户 1 也查不到这一行
        assertThat(appointmentService.queryByIdForUser(id, 1)).isNull();
    }

    @Test
    @DisplayName("取消后可重新预约同一时段")
    void rebookAfterCancelAllowed() {
        LocalDate date = LocalDate.now().plusDays(7);
        BookingResult first = appointmentService.submit(
                booking(1, 2, date, "下午 15:30-16:30", TEST_PATIENT));
        assertThat(first.isOk()).isTrue();
        assertThat(appointmentService.cancel(first.getAppointment().getId(), 2)).isTrue();

        BookingResult again = appointmentService.submit(
                booking(1, 2, date, "下午 15:30-16:30", TEST_PATIENT));
        assertThat(again.isOk()).isTrue();
    }

    // ---------------------------------------------------------------- 医生端

    @Test
    @DisplayName("医生端按日期筛选：只看到自己的预约")
    void doctorSeesOnlyOwnAppointments() {
        LocalDate date = LocalDate.now().plusDays(8);
        // 李娜（id 2）与张伟（id 1）同一天各一条
        BookingResult mine = appointmentService.submit(
                booking(2, 2, date, "上午 08:00-09:00", TEST_PATIENT));
        BookingResult other = appointmentService.submit(
                booking(1, 2, date, "上午 08:00-09:00", TEST_PATIENT));
        assertThat(mine.isOk()).isTrue();
        assertThat(other.isOk()).isTrue();

        List<Appointment> list = appointmentService.queryByDoctorAndDate(2, date, null);

        assertThat(list).isNotEmpty();
        assertThat(list).allSatisfy(appt -> assertThat(appt.getDoctorId()).isEqualTo(2));
        assertThat(list).extracting(Appointment::getId).contains(mine.getAppointment().getId());
        assertThat(list).extracting(Appointment::getId).doesNotContain(other.getAppointment().getId());
    }

    @Test
    @DisplayName("医生端按状态筛选与日期区间筛选")
    void doctorFilterByStatusAndRange() {
        LocalDate date = LocalDate.now().plusDays(9);
        BookingResult keep = appointmentService.submit(
                booking(2, 2, date, "上午 09:00-10:00", TEST_PATIENT));
        BookingResult drop = appointmentService.submit(
                booking(2, 2, date, "上午 10:00-11:00", TEST_PATIENT));
        appointmentService.cancel(drop.getAppointment().getId(), 2);

        List<Appointment> pending = appointmentService.queryByDoctorAndDate(2, date, 0);
        assertThat(pending).extracting(Appointment::getId).contains(keep.getAppointment().getId());
        assertThat(pending).extracting(Appointment::getId).doesNotContain(drop.getAppointment().getId());

        List<Appointment> cancelled = appointmentService.queryByDoctorAndDate(2, date, 1);
        assertThat(cancelled).extracting(Appointment::getId).contains(drop.getAppointment().getId());

        List<Appointment> ranged = appointmentService.queryByDoctorAndDateRange(
                2, LocalDate.now(), LocalDate.now().plusDays(30), null);
        assertThat(ranged).extracting(Appointment::getId).contains(keep.getAppointment().getId());
    }

    @Test
    @DisplayName("医生端会话不带 doctorId 时返回空列表而不是全量")
    void doctorQueryWithoutDoctorIdReturnsEmpty() {
        assertThat(appointmentService.queryByDoctorAndDate(null, LocalDate.now(), null)).isEmpty();
        assertThat(appointmentService.queryByDoctorAndDateRange(null, LocalDate.now(),
                LocalDate.now().plusDays(1), null)).isEmpty();
    }

    // ---------------------------------------------------------------- 工具

    private int countAppointments() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_appointment WHERE patient_name = ?", Integer.class, TEST_PATIENT);
        return count == null ? 0 : count;
    }

    private UserCredential credential(String username, String password, String loginType) {
        UserCredential credential = new UserCredential();
        credential.setUsername(username);
        credential.setPassword(password);
        credential.setLoginType(loginType);
        return credential;
    }

    private Appointment booking(int doctorId, int userId, LocalDate date, String slot, String patientName) {
        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctorId);
        appointment.setUserId(userId);
        appointment.setVisitDate(date);
        appointment.setTimeSlot(slot);
        appointment.setPatientName(patientName);
        appointment.setPatientGender("男");
        appointment.setPatientPhone("13900000000");
        appointment.setPatientIdCard("330106199001010011");
        appointment.setRemark("单元测试自动创建");
        return appointment;
    }

    /** 与实现保持同一个上限常量，避免测试里硬编码数字漂移 */
    private static final class AppointmentServiceImplLimits {
        private static final int MAX_ADVANCE_DAYS = 14;
    }
}
