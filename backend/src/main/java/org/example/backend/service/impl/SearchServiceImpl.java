package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.DoctorVO;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.Department;
import org.example.backend.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    public Map<String, Object> searchByKeyword(String keyword) {
        // 1️⃣ 查询医生（匹配医生名、科室名、描述）
        List<DoctorVO> doctorResults = doctorMapper.searchDoctors(keyword);

        // 2️⃣ 查询科室（仅匹配科室名）
        List<Department> deptResults = departmentMapper.selectList(
                new QueryWrapper<Department>().like("dept_name", keyword)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctorResults);
        result.put("departments", deptResults);
        return result;
    }
}
