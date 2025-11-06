package org.example.backend.service.impl;

import jakarta.annotation.Resource;
import org.example.backend.dto.DepartmentTreeVO;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.pojo.Department;
import org.example.backend.service.DepartmentService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    /**
     * 获取可预约科室树（假设全部科室都可预约）
     */
    @Override
    public List<DepartmentTreeVO> getAvailableDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(null);
        return buildDepartmentTree(allDepartments);
    }

    /**
     * 获取所有科室树结构
     */
    @Override
    public List<DepartmentTreeVO> getAllDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(null);
        return buildDepartmentTree(allDepartments);
    }

    /**
     * 新增科室
     */
    @Override
    public void addDepartment(Department department) {
        departmentMapper.insert(department);
    }

    /**
     * 修改科室
     */
    @Override
    public void updateDepartment(Department department) {
        departmentMapper.updateById(department);
    }

    /**
     * 删除科室
     */
    @Override
    public boolean deleteDepartment(Integer id) {
        int affectedRows = departmentMapper.deleteById(id);
        return affectedRows > 0;
    }

    /**
     * 构建科室树（一级科室 → 二级科室）
     */
    private List<DepartmentTreeVO> buildDepartmentTree(List<Department> allDepartments) {
        List<DepartmentTreeVO> roots = allDepartments.stream()
                .filter(d -> d.getParentDeptId() == null)
                .map(this::toVO)
                .collect(Collectors.toList());

        for (DepartmentTreeVO root : roots) {
            List<DepartmentTreeVO> children = allDepartments.stream()
                    .filter(d -> root.getDeptId().equals(d.getParentDeptId()))
                    .map(this::toVO)
                    .collect(Collectors.toList());
            root.setChildren(children);
        }
        return roots;
    }

    private DepartmentTreeVO toVO(Department department) {
        DepartmentTreeVO vo = new DepartmentTreeVO();
        vo.setDeptId(department.getDeptId());
        vo.setDeptName(department.getDeptName());
        vo.setParentDeptId(department.getParentDeptId());
        vo.setBuilding(department.getBuilding());
        vo.setFloor(department.getFloor());
        vo.setRoom(department.getRoom());
        vo.setDescription(department.getDescription());
        return vo;
    }
}
