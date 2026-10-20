package com.zjsru.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 统一 JSON 响应，T 为业务数据类型；Lombok 生成访问器及无参、全参构造。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    // 业务状态码，与 HTTP 状态独立；异常响应的 HTTP 状态仍为 200。
    private int code;
    // 成功提示或异常详情，序列化为 JSON 的 msg 字段。
    private String msg;
    // 成功时携带业务数据，失败时显式输出 JSON null。
    private T data;

    /** 泛型工厂方法：根据传入数据推断 T，并封装成功结果。 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 失败结果使用业务码 500，保留消息且不携带业务数据。 */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }
}
