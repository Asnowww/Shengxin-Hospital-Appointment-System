package org.example.backend.service;

import org.example.backend.dto.DepartmentTreeVO;

import java.util.List;

public interface DepartmentService {
    List<DepartmentTreeVO> getAvailableDepartmentTree();

    List<DepartmentTreeVO> getAllDepartmentTree();
}
