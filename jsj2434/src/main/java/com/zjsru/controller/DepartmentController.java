package com.zjsru.controller;

import com.zjsru.domain.Department;
import com.zjsru.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DepartmentController {
    @Autowired
    DepartmentService departmentService;

    //科室 + 医生关联查询（JSON）：/queryDepartmentInfoAndDoctorsByDeptId?deptId=1
    //参数用包装类型 Integer 而不是裸 int：不传 deptId 时返回 null（200），不会 500
    @RequestMapping("/queryDepartmentInfoAndDoctorsByDeptId")
    @ResponseBody
    public Department queryDepartmentInfoAndDoctorsByDeptId(
            @RequestParam(value = "deptId", required = false) Integer deptId) {
        return departmentService.queryDepartmentInfoAndDoctorsByDeptId(deptId);
    }

    //科室列表页面：每个科室带上自己的医生集合，由模板 departmentList.html 渲染
    @RequestMapping("/departmentList")
    public ModelAndView departmentList(ModelAndView modelAndView) {
        List<Department> departments = departmentService.queryAllDepartments();
        List<Department> result = new ArrayList<>();
        for (Department dept : departments) {
            //逐个科室取关联的医生，保证页面和 JSON 接口走同一套关联查询
            Department detail = departmentService.queryDepartmentInfoAndDoctorsByDeptId(dept.getId());
            result.add(detail != null ? detail : dept);
        }
        modelAndView.addObject("departments", result);
        modelAndView.setViewName("departmentList");
        return modelAndView;
    }
}
