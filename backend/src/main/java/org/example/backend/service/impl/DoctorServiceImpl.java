package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.DoctorAttendanceStats;
import org.example.backend.dto.DoctorVO;
import org.example.backend.dto.DoctorWorkloadStats;
import org.example.backend.mapper.*;
import org.example.backend.pojo.Department;
import org.example.backend.pojo.Doctor;
import org.example.backend.pojo.DoctorBioUpdateRequest;
import org.example.backend.pojo.User;
import org.example.backend.service.DoctorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorBioUpdateRequestMapper doctorBioUpdateRequestMapper;

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
    public List<DoctorVO> getDoctorVOByDeptId(Integer deptId) {
        // 1. 查询指定科室的医生
        List<Doctor> doctors = doctorMapper.selectList(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getDeptId, deptId)
        );

        if (doctors.isEmpty()) return Collections.emptyList();

        // 2. 获取医生对应的 userId 和 deptId 列表
        List<Long> userIds = doctors.stream().map(Doctor::getUserId).distinct().toList();
        List<Integer> deptIds = doctors.stream().map(Doctor::getDeptId).distinct().toList();

        // 3. 查询用户表获取医生姓名
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));

        // 4. 查询科室表获取科室名称
        Map<Integer, String> deptNameMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getDeptId, Department::getDeptName));

        // 5. 构建 DoctorVO 列表返回
        return doctors.stream().map(d -> {
            DoctorVO vo = new DoctorVO();
            vo.setDoctorId(d.getDoctorId());
            vo.setDeptId(d.getDeptId());
            vo.setDoctorName(userNameMap.get(d.getUserId()));
            vo.setDeptName(deptNameMap.get(d.getDeptId()));
            vo.setTitle(d.getTitle());
            vo.setBio(d.getBio());
            // vo.setStatus(d.getStatus()); // 如果需要状态也可以放开
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


    /**
     * 统计每个医生的工作量（排班数、预约数、空余号源数）
     */
    @Override
    public List<DoctorWorkloadStats> getDoctorWorkloadStats(LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectDoctorWorkloadStats(startDate, endDate);
    }

    /**
     * 统计医生的出诊率（实际出诊次数/计划排班次数）
     */
    @Override
    public List<DoctorAttendanceStats> getDoctorAttendanceStats(LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectDoctorAttendanceStats(startDate, endDate);
    }

    @Override
    public DoctorVO getDoctorById(Long userId) {

        // 1. 根据 userId 查询 doctor
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, userId)
        );
        if (doctor == null) return null;

        Long doctorId = doctor.getDoctorId();

        // 2. 查询用户信息
        User user = userMapper.selectById(userId);

        // 3. 查询科室信息
        Department dept = departmentMapper.selectById(doctor.getDeptId());

        DoctorBioUpdateRequest latestRequest = doctorBioUpdateRequestMapper.selectOne(
                new LambdaQueryWrapper<DoctorBioUpdateRequest>()
                        .eq(DoctorBioUpdateRequest::getDoctorId, doctorId)
                        .orderByDesc(DoctorBioUpdateRequest::getCreatedAt)
                        .last("LIMIT 1")
        );


        // 5. 组装 VO
        DoctorVO vo = new DoctorVO();
        vo.setDoctorId(doctorId);
        vo.setDeptId(doctor.getDeptId());
        vo.setEmail(user != null ? user.getEmail() : null);
        vo.setPhone(user != null ? user.getPhone() : null);
        vo.setDoctorName(user != null ? user.getUsername() : null);
        vo.setDeptName(dept != null ? dept.getDeptName() : null);
        vo.setTitle(doctor.getTitle());
        vo.setBio(doctor.getBio());
        vo.setStatus(user != null ? user.getStatus() : null);

        // 新增：返回最新审核状态
        // 若没有记录，则设为 null 或自定义值（例如 -1 表示无申请）
        vo.setBioStatus(latestRequest != null ? latestRequest.getStatus() : null);

        return vo;


    }

    @Override
    public DoctorVO getDoctorByDoctorId(Long doctorId) {

    Doctor doctor = doctorMapper.selectById(doctorId);

        // 3. 查询科室信息
        Department dept = departmentMapper.selectById(doctor.getDeptId());

        DoctorBioUpdateRequest latestRequest = doctorBioUpdateRequestMapper.selectOne(
                new LambdaQueryWrapper<DoctorBioUpdateRequest>()
                        .eq(DoctorBioUpdateRequest::getDoctorId, doctorId)
                        .orderByDesc(DoctorBioUpdateRequest::getCreatedAt)
                        .last("LIMIT 1")
        );


        // 5. 组装 VO
        DoctorVO vo = new DoctorVO();
        vo.setDoctorId(doctorId);
        vo.setDeptId(doctor.getDeptId());

//        vo.setDoctorName(doctor : null);
        vo.setDeptName(dept != null ? dept.getDeptName() : null);
        vo.setTitle(doctor.getTitle());
        vo.setBio(doctor.getBio());
//        vo.setStatus(user != null ? user.getStatus() : null);

        // 新增：返回最新审核状态
        // 若没有记录，则设为 null 或自定义值（例如 -1 表示无申请）
        vo.setBioStatus(latestRequest != null ? latestRequest.getStatus() : null);

        return vo;
    }
}
