package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

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

    @Override
    public void addDoctor(DoctorVO doctorVO){
        // 1. 校验科室是否存在
        Department dept = departmentMapper.selectOne(
                new QueryWrapper<Department>().eq("dept_name", doctorVO.getDeptName())
        );
        if (dept == null) throw new RuntimeException("科室不存在");

        // 2. 创建用户表记录
        User user = new User();
        user.setUsername(doctorVO.getDoctorName());
        user.setPassword("123456"); // 必填，设置默认密码
        user.setPhone("12345678910"); // 必填，设置默认手机号
        user.setRoleType("doctor");
        user.setStatus("verified"); //医生无需认证
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 3. 创建医生表记录
        Doctor doctor = new Doctor();
        doctor.setUserId(user.getUserId());
        doctor.setDeptId(dept.getDeptId());
        doctor.setTitle(doctorVO.getTitle());
        doctor.setBio(doctorVO.getBio());
        doctor.setStatus("active");
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());
        this.save(doctor);
    }

    @Override
    public void updateDoctorInfo(DoctorVO doctorVO) {
        // 1. 查询医生对应的用户ID
        Doctor doctor = this.getById(doctorVO.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        // 2. 更新 users 表的 username
        User user = new User();
        user.setUserId(doctor.getUserId());
        user.setUsername(doctorVO.getDoctorName());
        userMapper.updateById(user);

        // 3. 根据科室名字查 deptId
        Department dept = departmentMapper.selectOne(
                new QueryWrapper<Department>().eq("dept_name", doctorVO.getDeptName())
        );
        if (dept == null) {
            throw new RuntimeException("科室不存在");
        }

        // 4. 更新 doctors 表
        doctor.setDeptId(dept.getDeptId());
        doctor.setTitle(doctorVO.getTitle());
        doctor.setBio(doctorVO.getBio());
        doctor.setUpdatedAt(LocalDateTime.now());
        this.updateById(doctor);
    }
}
