package com.zjsru.controller;

import com.zjsru.domain.Doctor;
import com.zjsru.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class DoctorController {
    @Autowired
    DoctorService doctorService;
    //查询所有医生信息
    @RequestMapping("/queryAllDoctors")
    public ModelAndView queryAllDoctors() {
        List<Doctor> doctors = doctorService.queryAllDoctors();
        ModelAndView modelAndView = new ModelAndView("doctorList");
        modelAndView.addObject("doctors", doctors);
        return modelAndView;
    }
    //根据医生ID查询医生信息
    @RequestMapping("/queryDoctorById")
    @ResponseBody
    public Doctor queryDoctorById(int id) {
        Doctor doctor = doctorService.queryDoctorById(id);
        return doctor;
    }
    //添加医生信息
    @RequestMapping("/addDoctor")
    @ResponseBody
    public int addDoctor(Doctor doctor) {
        return doctorService.addDoctor(doctor);
    }
    //根据动态条件查询医生信息
    @RequestMapping("/queryDoctorsByCondition")
    @ResponseBody
    public List<Doctor> queryDoctorsByCondition(Doctor doctor) {
        return doctorService.queryDoctorsByCondition(doctor);
    }
    //根据单个条件查询医生信息
    @RequestMapping("/queryDoctorsBySingleCondition")
    @ResponseBody
    public List<Doctor> queryDoctorsBySingleCondition(Doctor doctor) {
        return doctorService.queryDoctorsBySingleCondition(doctor);
    }
    //根据医生ID查询医生信息和部门信息
    @RequestMapping("/queryDoctorInfoAndDepartmentInfo")
    @ResponseBody
    public Doctor queryDoctorInfoAndDepartmentInfo(int id) {
        return doctorService.queryDoctorInfoAndDepartmentInfo(id);
    }
}
