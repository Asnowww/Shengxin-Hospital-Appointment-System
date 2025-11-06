package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.DepartmentTreeVO;
import org.example.backend.pojo.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {
    List<DepartmentTreeVO> getAvailableDepartmentTree();

    List<DepartmentTreeVO> getAllDepartmentTree();

    void addDepartment(Department department);
    void updateDepartment(Department department);
    boolean deleteDepartment(Integer id);

}
