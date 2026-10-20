package com.zjsru.exception;

import com.zjsru.domain.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 捕获业务控制器异常，返回对象由 Spring 自动序列化为 JSON，而非解析为模板名。 */
// 限定控制器包，静态资源不存在等框架错误仍按原有规则处理。
@RestControllerAdvice(basePackages = "com.zjsru.controller")
public class GlobalExceptionHandler {

    /** 处理空引用访问；Spring 优先选择匹配类型更具体的异常处理方法。 */
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointer(NullPointerException e) {
        // 课程写法：在控制台输出堆栈，响应中只封装消息而非完整堆栈。
        e.printStackTrace();
        return Result.fail("空指针异常：" + e.getMessage());
    }

    /** 处理整数除零等算术错误，响应消息保留异常详情。 */
    @ExceptionHandler(ArithmeticException.class)
    public Result<String> handleArithmetic(ArithmeticException e) {
        // 课程写法：在控制台输出堆栈，响应中只封装消息而非完整堆栈。
        e.printStackTrace();
        return Result.fail("算术异常：" + e.getMessage());
    }

    /** 兜底处理未被上述方法匹配的 Exception，不处理 JVM 的 Error。 */
    @ExceptionHandler(Exception.class)
    public Result<String> handleOther(Exception e) {
        // 课程写法：在控制台输出堆栈，响应中只封装消息而非完整堆栈。
        e.printStackTrace();
        return Result.fail("其他异常：" + e.getMessage());
    }
}
