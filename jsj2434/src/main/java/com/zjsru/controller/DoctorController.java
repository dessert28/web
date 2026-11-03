package com.zjsru.controller;

import com.zjsru.domain.Doctor;
import com.zjsru.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class DoctorController {
    @Autowired
    DoctorService doctorService;
    //查询所有医生信息
    @RequestMapping("/queryAllDoctors")
    @ResponseBody
    public List<Doctor> queryAllDoctors() {
        List<Doctor> doctors = doctorService.queryAllDoctors();
        return doctors;
    }
    //跳转医生列表页面：把医生集合放入模型，由模板 doctorList.html 渲染成表格
    @RequestMapping("/doctorList")
    public ModelAndView doctorList(ModelAndView modelAndView) {
        //1.调用业务逻辑方法
        List<Doctor> doctors = doctorService.queryAllDoctors();
        //2.把数据交给模板，并指定视图名
        modelAndView.addObject("doctors", doctors);
        modelAndView.setViewName("doctorList");
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
    //条件查询（JSON）：表单参数绑定到 Doctor，返回符合条件的医生列表
    @RequestMapping("/queryDoctorsByCondition")
    @ResponseBody
    public List<Doctor> queryDoctorsByCondition(Doctor doctor) {
        //1.调用业务逻辑方法
        List<Doctor> doctors = doctorService.queryDoctorsByCondition(doctor);
        //2.返回结果给前端
        return doctors;
    }
    //条件查询（页面）：把条件和结果一起交给 doctorQuery.html，输入框回显上一次的查询条件
    @RequestMapping("/doctorQuery")
    public ModelAndView doctorQuery(Doctor doctor, ModelAndView modelAndView) {
        //1.调用业务逻辑方法
        List<Doctor> doctors = doctorService.queryDoctorsByCondition(doctor);
        //2.返回结果给前端
        modelAndView.addObject("doctors", doctors);
        modelAndView.addObject("condition", doctor);
        modelAndView.setViewName("doctorQuery");
        return modelAndView;
    }
    //根据单个条件查询医生信息：choose 只取第一个成立的条件，都不填时默认查主任医师
    @RequestMapping("/queryDoctorsBySingleCondition")
    @ResponseBody
    public List<Doctor> queryDoctorsBySingleCondition(Doctor doctor) {
        //1.调用业务逻辑方法
        List<Doctor> doctors = doctorService.queryDoctorsBySingleCondition(doctor);
        //2.返回结果给前端
        return doctors;
    }
    //动态更新医生信息：只提交需要改的字段，未提交的字段不会被清空
    @RequestMapping("/updateDoctor")
    @ResponseBody
    public int updateDoctor(Doctor doctor) {
        //没传 id 或一个可更新字段都没传时直接返回 0：
        //否则 <set> 里什么都不会生成，拼出来的 SQL 是 update doctor_info where id=?，语法错误
        if (doctor.getId() == null || !hasAnyUpdateField(doctor)) {
            return 0;
        }
        //1.调用业务逻辑方法
        int rows = doctorService.updateDoctor(doctor);
        //2.返回受影响行数
        return rows;
    }
    //判断是否至少提交了一个可更新字段
    private boolean hasAnyUpdateField(Doctor doctor) {
        return notBlank(doctor.getDoctorName()) || notBlank(doctor.getGender())
                || notBlank(doctor.getDepartment()) || notBlank(doctor.getTitle())
                || notBlank(doctor.getPhone());
    }

    private boolean notBlank(String value) {
        return value != null && value.trim().length() > 0;
    }
    //根据多个医生ID批量查询：/queryDoctorsByIds?ids=1,2,3
    @RequestMapping("/queryDoctorsByIds")
    @ResponseBody
    public List<Doctor> queryDoctorsByIds(@RequestParam("ids") List<Integer> ids) {
        //1.调用业务逻辑方法
        List<Doctor> doctors = doctorService.queryDoctorsByIds(ids);
        //2.返回结果给前端
        return doctors;
    }
}
