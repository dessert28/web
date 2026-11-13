package com.zjsru.hospital.service;

import com.zjsru.hospital.domain.Department;
import com.zjsru.hospital.domain.DepartmentSearchResult;
import com.zjsru.hospital.domain.Doctor;
import com.zjsru.hospital.mapper.DepartmentMapper;
import com.zjsru.hospital.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<Department> queryAllDepartments() {
        return departmentMapper.queryAllDepartments();
    }

    @Override
    public Department queryDepartmentInfoAndDoctorsByDeptId(Integer deptId) {
        if (deptId == null) {
            return null;
        }
        return departmentMapper.queryDepartmentInfoAndDoctorsByDeptId(deptId);
    }

    @Override
    public Department queryDepartmentById(Integer deptId) {
        if (deptId == null) {
            return null;
        }
        return departmentMapper.queryDepartmentById(deptId);
    }

    @Override
    public List<DepartmentSearchResult> search(String keyword) {
        List<DepartmentSearchResult> results = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            // 没给关键字就返回全部科室，前端行为一致
            for (Department department : departmentMapper.queryAllDepartments()) {
                results.add(new DepartmentSearchResult(department, List.of()));
            }
            return results;
        }

        String kw = keyword.trim();
        Map<Integer, Department> deptMap = new LinkedHashMap<>();
        Map<Integer, List<Doctor>> matchedDoctors = new LinkedHashMap<>();

        // 1) 科室本身就命中
        for (Department department : departmentMapper.queryDepartmentsByKeyword(kw)) {
            deptMap.put(department.getId(), department);
        }

        // 2) 医生命中，则把医生所属科室带出来
        for (Doctor doctor : doctorMapper.queryDoctorsByKeyword(kw)) {
            Integer deptId = doctor.getDepartmentId();
            if (deptId == null) {
                continue;
            }
            if (!deptMap.containsKey(deptId)) {
                Department department = departmentMapper.queryDepartmentById(deptId);
                if (department == null) {
                    continue;
                }
                deptMap.put(deptId, department);
            }
            matchedDoctors.computeIfAbsent(deptId, k -> new ArrayList<>()).add(doctor);
        }

        for (Map.Entry<Integer, Department> entry : deptMap.entrySet()) {
            results.add(new DepartmentSearchResult(entry.getValue(),
                    matchedDoctors.getOrDefault(entry.getKey(), List.of())));
        }
        return results;
    }
}
