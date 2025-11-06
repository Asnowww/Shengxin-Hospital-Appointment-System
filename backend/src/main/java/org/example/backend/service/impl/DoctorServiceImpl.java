package org.example.backend.service.impl;

import jakarta.annotation.Resource;
import org.example.backend.dto.DoctorVO;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.Department;
import org.example.backend.pojo.Doctor;
import org.example.backend.pojo.User;
import org.example.backend.service.DoctorService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    public List<DoctorVO> getAllDoctorsWithNameAndDept() {
        List<Doctor> doctors = doctorMapper.selectList(null);
        if (doctors.isEmpty()) return Collections.emptyList();

        List<Long> userIds = doctors.stream().map(Doctor::getUserId).distinct().toList();
        List<Integer> deptIds = doctors.stream().map(Doctor::getDeptId).distinct().toList();

        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));

        Map<Integer, String> deptNameMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getDeptId, Department::getDeptName));

        return doctors.stream().map(d -> {
            DoctorVO vo = new DoctorVO();
            vo.setDoctorId(d.getDoctorId());
            vo.setDeptId(d.getDeptId());
            vo.setDoctorName(userNameMap.get(d.getUserId()));
            vo.setDeptName(deptNameMap.get(d.getDeptId()));
            vo.setTitle(d.getTitle());
            vo.setBio(d.getBio());
//            vo.setStatus(d.getStatus());
            return vo;
        }).toList();
    }

}
