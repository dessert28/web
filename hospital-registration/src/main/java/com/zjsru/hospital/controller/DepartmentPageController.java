package com.zjsru.hospital.controller;

import com.zjsru.hospital.domain.Department;
import com.zjsru.hospital.domain.DepartmentSearchResult;
import com.zjsru.hospital.interceptor.LoginInterceptor;
import com.zjsru.hospital.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 患者端科室浏览与检索（首页无需登录）。
 */
@Controller
public class DepartmentPageController {

    @Autowired
    private DepartmentService departmentService;

    /** 首页：直接展示全部科室，允许匿名访问 */
    @GetMapping({"/", "/home"})
    public ModelAndView home(HttpSession session) {
        ModelAndView mv = new ModelAndView("patientHome");
        List<Department> departments = departmentService.queryAllDepartments();
        mv.addObject("departments", departments);
        mv.addObject("keyword", "");
        mv.addObject("loginUser", session.getAttribute(LoginInterceptor.SESSION_USER));
        return mv;
    }

    /** 科室/门诊检索：科室名、位置、电话，或医生姓名/职称/专长 */
    @GetMapping("/search")
    public ModelAndView search(@RequestParam(value = "keyword", required = false) String keyword,
                               HttpSession session) {
        ModelAndView mv = new ModelAndView("patientHome");
        String kw = keyword == null ? "" : keyword.trim();
        List<DepartmentSearchResult> results = departmentService.search(kw);
        mv.addObject("results", results);
        mv.addObject("departments", departmentService.queryAllDepartments());
        mv.addObject("keyword", kw);
        mv.addObject("searchMode", true);
        mv.addObject("loginUser", session.getAttribute(LoginInterceptor.SESSION_USER));
        return mv;
    }

    /** 科室详情 + 该科室医生列表（需登录，由 LoginInterceptor 保证） */
    @GetMapping("/department/{deptId}")
    public ModelAndView departmentDetail(@PathVariable("deptId") Integer deptId, HttpSession session) {
        ModelAndView mv = new ModelAndView("departmentDetail");
        Department department = departmentService.queryDepartmentInfoAndDoctorsByDeptId(deptId);
        mv.addObject("department", department);
        mv.addObject("deptId", deptId);
        mv.addObject("loginUser", session.getAttribute(LoginInterceptor.SESSION_USER));
        if (department == null) {
            mv.addObject("message", "该科室不存在或已下线");
        }
        return mv;
    }
}
