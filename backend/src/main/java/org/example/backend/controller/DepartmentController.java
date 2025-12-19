package org.example.backend.controller;

import org.example.backend.dto.*;
import org.example.backend.pojo.Department;
import org.example.backend.service.DepartmentService;
import org.springframework.web.bind.annotation.*;
import org.example.backend.dto.Result;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
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
     * 删除科室（使用POST请求）
     * 前端：管理员点击删除按钮后调用
     */
    @PostMapping("/delete/{id}")
    public Result<String> deleteDepartment(@PathVariable Integer id) {
        try {
            boolean success = departmentService.deleteDepartment(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败：科室不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    /**
     * 获取所有可用的科室区域
     * GET /api/departments/available-areas
     */
    @GetMapping("/available-areas")
    public Result<List<DeptAreaDTO>> getAvailableAreas() {
        try {
            List<DeptAreaDTO> areas = departmentService.getAvailableAreas();
            return Result.success(areas);
        } catch (Exception e) {
            return Result.error("获取可用区域失败: " + e.getMessage());
        }
    }

    /**
     * 创建新科室并分配诊室（完整版本）
     * POST /api/departments/create-with-rooms
     *
     * 请求体示例:
     * {
     *   "parentDeptId": 1,
     *   "deptName": "心血管内科",
     *   "building": "圣心楼",
     *   "floor": 3,
     *   "mainRoom": "301",
     *   "roomNumbers": ["302", "303", "304", "305"],
     *   "description": "心血管内科专注于心脏疾病的诊治"
     * }
     */
    @PostMapping("/create-with-rooms")
    public Result<DeptCreateResult> createDepartmentWithRooms(@RequestBody DeptCreateParam param) {
        try {
            DeptCreateResult result = departmentService.createDepartmentWithRooms(param);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有没有医生的科室列表
     * GET /api/departments/empty
     * <p>
     * 用于提醒管理员为这些科室分配医生
     */
    @GetMapping("/empty")
    public Result getEmptyDepartments() {
        try {
            List<EmptyDeptDTO> emptyDepts = departmentService.getEmptyDepartments();
            String message = emptyDepts.isEmpty()
                    ? "所有科室都已分配医生"
                    : "有 " + emptyDepts.size() + " 个科室尚未分配医生";
            return Result.success(emptyDepts.toString(), message);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取空科室数量（用于首页提醒）
     * GET /api/departments/empty-count
     */
    @GetMapping("/empty-count")
    public Result<Integer> getEmptyDepartmentCount() {
        try {
            List<EmptyDeptDTO> emptyDepts = departmentService.getEmptyDepartments();
            return Result.success(emptyDepts.size());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取科室详细信息
     * 前端：编辑科室时获取当前科室信息
     */
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Integer id) {
        try {
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                return Result.error("科室不存在");
            }
            return Result.success(department);
        } catch (Exception e) {
            return Result.error("获取科室信息失败: " + e.getMessage());
        }
    }


}
