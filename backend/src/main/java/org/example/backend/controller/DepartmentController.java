package org.example.backend.controller;

import org.example.backend.dto.DepartmentTreeVO;
import org.example.backend.dto.Result;
import org.example.backend.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/departments")
@CrossOrigin
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    /**
     * 获取可预约科室树
     * 前端：显示一级科室 → 二级科室列表
     */
    @GetMapping("/available")
    public Result<List<DepartmentTreeVO>> getAvailableDepartments() {
        try {
            List<DepartmentTreeVO> departmentTree = departmentService.getAvailableDepartmentTree();
            return Result.success(departmentTree);
        } catch (Exception e) {
            return Result.error("获取科室列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public Result<List<DepartmentTreeVO>> getAllDepartments() {
        try {
            List<DepartmentTreeVO> departmentTree = departmentService.getAllDepartmentTree();
            return Result.success(departmentTree);
        } catch (Exception e) {
            return Result.error("获取所有科室失败: " + e.getMessage());
        }
    }

}
