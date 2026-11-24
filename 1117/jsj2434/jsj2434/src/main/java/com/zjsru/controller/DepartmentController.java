package com.zjsru.controller;

import com.zjsru.domain.Department;
import com.zjsru.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    //根据部门ID查询部门信息及部门下的所有医生信息
    @RequestMapping("/queryDepartmentAndDoctors")
    @ResponseBody
    public Department queryDepartmentAndDoctors(int deptId) {
        return departmentService.queryDepartmentInfoAndDoctorsByDeptId(deptId);
    }
}
