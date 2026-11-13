package com.zjsru.hospital.domain;

import java.util.List;

/**
 * 科室检索结果：一个科室 + 命中的医生（检索医生姓名时用来提示命中了谁）。
 */
public class DepartmentSearchResult {
    private final Department department;
    private final List<Doctor> matchedDoctors;

    public DepartmentSearchResult(Department department, List<Doctor> matchedDoctors) {
        this.department = department;
        this.matchedDoctors = matchedDoctors;
    }

    public Department getDepartment() {
        return department;
    }

    public List<Doctor> getMatchedDoctors() {
        return matchedDoctors;
    }

    public boolean isMatchedByDoctor() {
        return matchedDoctors != null && !matchedDoctors.isEmpty();
    }
}
