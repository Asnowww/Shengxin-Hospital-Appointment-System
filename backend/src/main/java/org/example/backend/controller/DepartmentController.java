package org.example.backend.controller;

import org.example.backend.dto.DepartmentTreeVO;
import org.example.backend.dto.Result;
import org.example.backend.pojo.Department;
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

    /**
     * 新增科室
     * 前端：管理员点击“新增科室”按钮后调用
     */
    @PostMapping
    public Result<String> addDepartment(@RequestBody Department department) {
        try {
            departmentService.addDepartment(department);
            return Result.success("新增科室成功");
        } catch (Exception e) {
            return Result.error("新增科室失败: " + e.getMessage());
        }
    }

    /**
     * 修改科室信息
     * 前端：管理员编辑科室信息后调用
     */
    @PutMapping("/{id}")
    public Result<String> updateDepartment(@PathVariable Integer id, @RequestBody Department department) {
        try {
            department.setDeptId(id);
            departmentService.updateDepartment(department);
            return Result.success("修改科室成功");
        } catch (Exception e) {
            return Result.error("修改科室失败: " + e.getMessage());
        }
    }

    /**
     * 删除科室
     * 前端：管理员点击删除按钮后调用
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDepartment(@PathVariable Integer id) {
        boolean success = departmentService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败：科室不存在");
        }
    }
}
