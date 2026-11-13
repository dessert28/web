package com.zjsru.hospital.domain;

/**
 * 挂号提交结果：把「成功 / 各种失败原因」收敛成一个返回值，
 * 控制器据此决定跳转成功页还是回表单回显错误。
 */
public class BookingResult {
    public enum Code {
        /** 提交成功，appointment 里是刚插入并回填了 id 的记录 */
        OK,
        /** 同医生 + 同日 + 同时段已有待就诊预约 */
        DUPLICATE,
        /** 医生不存在 */
        NO_DOCTOR,
        /** 日期不合法（早于今天或晚于可预约上限） */
        BAD_DATE,
        /** 时段不在白名单里 */
        BAD_SLOT,
        /** 必填项缺失 */
        MISSING_FIELD
    }

    private final Code code;
    private final String message;
    private final Appointment appointment;

    private BookingResult(Code code, String message, Appointment appointment) {
        this.code = code;
        this.message = message;
        this.appointment = appointment;
    }

    public static BookingResult ok(Appointment appointment) {
        return new BookingResult(Code.OK, "挂号成功", appointment);
    }

    public static BookingResult fail(Code code, String message) {
        return new BookingResult(code, message, null);
    }

    public boolean isOk() {
        return code == Code.OK;
    }

    public Code getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Appointment getAppointment() {
        return appointment;
    }
}
