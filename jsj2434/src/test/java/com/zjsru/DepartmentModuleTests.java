package com.zjsru;

import com.zjsru.domain.Department;
import com.zjsru.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Department 模块（科室 + 医生一对多关联查询）的接口测试。
 * 依赖真实 MySQL：需先建好 mybatis 库并导入仓库根 mybatis.sql
 * （其中包含 doctor_department 的科室种子数据与 doctor_info.department_id 回填）。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DepartmentModuleTests {

    // 种子数据里的科室与医生分布（见仓库根 mybatis.sql）
    private static final int SEED_DEPT_ID_WITH_MANY = 2;
    private static final int SEED_DEPT_DOCTOR_COUNT = 3;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DepartmentMapper departmentMapper;

    // 科室列表页能正常渲染
    @Test
    public void departmentListRendersPage() throws Exception {
        mockMvc.perform(get("/departmentList"))
                .andExpect(status().isOk())
                .andExpect(view().name("departmentList"))
                .andExpect(model().attributeExists("departments"));
    }

    // 关联查询：deptId=1（心内科）应返回科室信息，且医生集合里有张伟
    @Test
    public void queryDepartmentInfoAndDoctorsByDeptIdReturnsDepartmentWithDoctors() throws Exception {
        mockMvc.perform(get("/queryDepartmentInfoAndDoctorsByDeptId").param("deptId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deptName").value("心内科"))
                .andExpect(jsonPath("$.doctorList").isArray())
                .andExpect(jsonPath("$.doctorList[0].doctorName").value("张伟"));
    }

    // 不存在的科室返回 null 正文，而不是 500
    @Test
    public void queryWithUnknownDeptIdReturnsNull() throws Exception {
        mockMvc.perform(get("/queryDepartmentInfoAndDoctorsByDeptId").param("deptId", "9999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    // 不传 deptId 也返回 null 正文：参数是包装类型 Integer，不会因为缺参报 500
    @Test
    public void queryWithoutDeptIdReturnsNullNotServerError() throws Exception {
        mockMvc.perform(get("/queryDepartmentInfoAndDoctorsByDeptId"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    // Mapper 层：科室种子数据已导入
    @Test
    public void queryAllDepartmentsReturnsSeedData() {
        List<Department> departments = departmentMapper.queryAllDepartments();
        assertNotNull(departments, "科室列表不应为 null");
        assertFalse(departments.isEmpty(), "doctor_department 应有种子数据，请先导入仓库根 mybatis.sql");
        Department first = departments.get(0);
        assertNotNull(first.getDeptName(), "科室名不应为 null");
        assertNotNull(first.getDeptPhone(), "dept_phone 列应已建好并回填");
    }

    // 回归：LEFT JOIN 在「科室没有医生」时会产出一行全 NULL 的 doc 列，
    // resultMap 的 notNullColumn 必须挡住它，否则 doctorList 会变成一个字段全 null 的空壳 Doctor。
    // 这里临时插一个空科室，测完删掉，不依赖种子数据里恰好有空科室。
    @Test
    public void doctorListIsEmptyInsteadOfNullShellWhenDepartmentHasNoDoctor() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            int emptyDeptId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "insert into doctor_department (dept_name, location, dept_phone) values (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "__测试空科室__");
                ps.setString(2, "无");
                ps.setString(3, "000");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    emptyDeptId = keys.getInt(1);
                }
            }
            try {
                Department dept = departmentMapper.queryDepartmentInfoAndDoctorsByDeptId(emptyDeptId);
                assertNotNull(dept, "科室本身应能查到");
                assertTrue(dept.getDoctorList() == null || dept.getDoctorList().isEmpty(),
                        "无医生的科室，doctorList 必须是空集合，不能是 size=1 的空壳对象");
            } finally {
                try (PreparedStatement del = conn.prepareStatement(
                        "delete from doctor_department where id = ?")) {
                    del.setInt(1, emptyDeptId);
                    del.executeUpdate();
                }
            }
        }
    }

    // 一个科室对应多名医生时，collection 必须把多行聚合成多个 Doctor
    @Test
    public void doctorListAggregatesMultipleDoctors() {
        Department dept = departmentMapper.queryDepartmentInfoAndDoctorsByDeptId(SEED_DEPT_ID_WITH_MANY);
        assertNotNull(dept);
        assertNotNull(dept.getDoctorList());
        assertEquals(SEED_DEPT_DOCTOR_COUNT, dept.getDoctorList().size(),
                "儿科在种子数据里应有 3 名医生，说明一对多聚合正常");
    }
}
