package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.pojo.Department;
import org.example.backend.pojo.Schedule;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.mapper.ScheduleMapper;
import org.example.backend.service.DepartmentService;
import org.example.backend.dto.DepartmentTreeVO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Override
    public List<DepartmentTreeVO> getAvailableDepartmentTree() {

        // 1️⃣ 查询所有开放排班（status=open 且剩余号源 > 0）
        QueryWrapper<Schedule> query = new QueryWrapper<>();
        query.eq("status", "open").gt("available_slots", 0);
        List<Schedule> schedules = scheduleMapper.selectList(query);
        if (schedules.isEmpty()) return List.of();

        // 2️⃣ 获取所有涉及的二级科室 ID
        Set<Integer> subDeptIds = schedules.stream()
                .map(Schedule::getDeptId)
                .collect(Collectors.toSet());

        // 3️⃣ 查询这些二级科室信息
        List<Department> subDepts = departmentMapper.selectBatchIds(subDeptIds);
        if (subDepts.isEmpty()) return List.of();

        // 4️⃣ 获取这些二级科室对应的一级科室ID
        Set<Integer> parentIds = subDepts.stream()
                .map(Department::getParentDeptId)
                .collect(Collectors.toSet());

        List<Department> parentDepts = departmentMapper.selectBatchIds(parentIds);

        // 5️⃣ 按 parentDeptId 对二级科室分组

         Map<Integer, List<DepartmentTreeVO>> groupedByParent =
             subDepts.stream().collect(Collectors.groupingBy(
                 Department::getParentDeptId,
                 Collectors.mapping(d -> {
                     DepartmentTreeVO vo = new DepartmentTreeVO();
                     vo.setId(d.getDeptId());
                     vo.setName(d.getDeptName());
                     vo.setAvailable(true);
                     return vo;
                 }, Collectors.toList())
             ));

        // 6️⃣ 组装一级科室 → 二级科室结构
        List<DepartmentTreeVO> result = parentDepts.stream()
                .map(parent -> {
                    DepartmentTreeVO vo = new DepartmentTreeVO();
                    vo.setId(parent.getDeptId());
                    vo.setName(parent.getDeptName());
                    vo.setAvailable(true);
                    vo.setSubDepartments(groupedByParent.getOrDefault(parent.getDeptId(), List.of()));
                    return vo;
                })
                // 仅保留有可预约子科室的一级科室
                .filter(vo -> vo.getSubDepartments() != null && !vo.getSubDepartments().isEmpty())
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public List<DepartmentTreeVO> getAllDepartmentTree() {
        // 1️. 查询所有二级科室（parent_dept_id != null 或 0 表示一级科室）
        QueryWrapper<Department> subQuery = new QueryWrapper<>();
        subQuery.isNotNull("parent_dept_id").ne("parent_dept_id", 0);
        List<Department> subDepts = departmentMapper.selectList(subQuery);

        // 2️.获取所有一级科室 ID
        Set<Integer> parentIds = subDepts.stream()
                .map(Department::getParentDeptId)
                .collect(Collectors.toSet());
        List<Department> parentDepts = parentIds.isEmpty() ? List.of() : departmentMapper.selectBatchIds(parentIds);

        // 3️.按 parentDeptId 分组二级科室
        Map<Integer, List<DepartmentTreeVO>> groupedByParent = subDepts.stream()
                .collect(Collectors.groupingBy(
                        Department::getParentDeptId, // 按 parentDeptId 分组
                        Collectors.mapping(d -> {
                            DepartmentTreeVO vo = new DepartmentTreeVO();
                            vo.setId(d.getDeptId());
                            vo.setName(d.getDeptName());
                            vo.setAvailable(true);
                            vo.setParentId(d.getParentDeptId());
                            return vo;
                        }, Collectors.toList())
                ));


        // 4️.构建一级科室 → 二级科室结构
        return parentDepts.stream()
                .map(parent -> {
                    DepartmentTreeVO vo = new DepartmentTreeVO();
                    vo.setId(parent.getDeptId());
                    vo.setName(parent.getDeptName());
                    vo.setAvailable(true);
                    vo.setSubDepartments(groupedByParent.getOrDefault(parent.getDeptId(), List.of()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

}
