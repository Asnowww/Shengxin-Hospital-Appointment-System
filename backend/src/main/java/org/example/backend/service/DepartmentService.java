package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.*;
import org.example.backend.pojo.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {
    List<DepartmentTreeVO> getAvailableDepartmentTree();

    List<DepartmentTreeVO> getAllDepartmentTree();

    void addDepartment(Department department);
    void updateDepartment(Department department);
    boolean deleteDepartment(Integer id);

    /**
     * 获取所有可用的科室区域（按楼层分组）
     */
    List<DeptAreaDTO> getAvailableAreas();

    /**
     * 创建二级科室并分配诊室
     */
    DeptCreateResult createDepartmentWithRooms(DeptCreateParam param);

    /**
     * 获取所有没有医生的科室列表
     */
    List<EmptyDeptDTO> getEmptyDepartments();

    /**
     * 获取所有一级科室列表
     */
    List<Department> getParentDepartments();

    /**
     * 根据ID获取科室信息
     */
    Department getDepartmentById(Integer deptId);

}
