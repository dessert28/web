package com.zjsru.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.zjsru.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointer(NullPointerException e) {
        e.printStackTrace();
        return "空指针异常：对象为空";
    }

    @ExceptionHandler(ArithmeticException.class)
    public String handleArithmetic(ArithmeticException e) {
        e.printStackTrace();
        return "算术异常：计算出错";
    }

    @ExceptionHandler(Exception.class)
    public String handleOther(Exception e) {
        e.printStackTrace();
        return "其他异常：系统处理失败";
    }
}
