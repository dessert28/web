package com.zjsru.controller;

import com.zjsru.domain.Doctor;
import com.zjsru.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/** 医生信息控制器：所有方法都用 @ResponseBody 直接输出 JSON，不跳转模板。 */
@Controller
public class DoctorController {
    // 注入医生业务层实现，控制器只负责接收参数并调用服务层。
    @Autowired
    private DoctorService doctorService;

    /** 查询 doctor_info 表全部医生，List 由 Spring 序列化为 JSON 数组。
        注意：只写 @ResponseBody 没有路径映射时浏览器访问不到，必须先有 @GetMapping。 */
    @GetMapping("/queryAllDoctors")
    @ResponseBody
    public List<Doctor> queryAllDoctors() {
        //1.调用业务逻辑方法
        List<Doctor> doctors = doctorService.queryAllDoctors();
        //2.返回结果给前端
        return doctors;
    }

    /** 按路径 id 查询单个医生，路径占位符 id 绑定到方法参数。 */
    @GetMapping("/queryDoctorById/{id}")
    @ResponseBody
    public Doctor queryDoctorById(@PathVariable Integer id) {
        //1.调用业务逻辑方法
        Doctor doctor = doctorService.queryDoctorById(id);
        //2.返回结果给前端
        return doctor;
    }

    /** 按医生姓名查询，同名可能有多条，因此返回 List。 */
    @GetMapping("/queryDoctorsByName/{doctorName}")
    @ResponseBody
    public List<Doctor> queryDoctorsByName(@PathVariable String doctorName) {
        //1.调用业务逻辑方法
        List<Doctor> doctorList = doctorService.queryDoctorsByName(doctorName);
        //2.返回结果给前端
        return doctorList;
    }

    /** 新增医生：表单参数绑定到 Doctor，返回受影响行数。 */
    @PostMapping("/insertDoctor")
    @ResponseBody
    public int insertDoctor(Doctor doctor) {
        //1.调用业务逻辑方法
        int rows = doctorService.insertDoctor(doctor);
        //2.返回结果给前端
        return rows;
    }

    /** 修改医生：按 id 更新其余字段，返回受影响行数。 */
    @PutMapping("/updateDoctor")
    @ResponseBody
    public int updateDoctor(Doctor doctor) {
        //1.调用业务逻辑方法
        int rows = doctorService.updateDoctor(doctor);
        //2.返回结果给前端
        return rows;
    }

    /** 删除医生：按路径 id 删除，返回受影响行数。 */
    @DeleteMapping("/deleteDoctorById/{id}")
    @ResponseBody
    public int deleteDoctorById(@PathVariable Integer id) {
        //1.调用业务逻辑方法
        int rows = doctorService.deleteDoctorById(id);
        //2.返回结果给前端
        return rows;
    }
}
