package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.DoctorAccountDTO;
import org.example.backend.dto.DoctorQueryDTO;
import org.example.backend.mapper.AppointmentMapper;
import org.example.backend.mapper.DoctorBioUpdateRequestMapper;
import org.example.backend.pojo.Doctor;
import org.example.backend.pojo.DoctorBioUpdateRequest;
import org.example.backend.pojo.User;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.service.DoctorAccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现管理员端对医生账户的增删改查、启停用、密码重置等功能。
 */
@Service
public class DoctorAccountServiceImpl implements DoctorAccountService {

    @Autowired
    private DoctorMapper doctorMapper; // 操作 doctors 表

    @Autowired
    private UserMapper userMapper; // 操作 users 表（医生登录账号信息）

    @Resource
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // Spring Security 密码加密器

    private static final String DEFAULT_PASSWORD = "123456"; // 新账号默认密码

    @Resource
    private DoctorBioUpdateRequestMapper requestMapper;

    /**
     * 查询医生列表（支持条件筛选）
     */
    @Override
    public List<DoctorAccountDTO> getDoctorList(DoctorQueryDTO queryDTO) {
        // 直接查询返回 DTO
        return doctorMapper.selectDoctorList(queryDTO);
    }


    /**
     * 添加医生账号（同时创建 user 和 doctor 两张表的记录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 出错自动回滚
    public void addDoctor(DoctorAccountDTO doctorDTO) {
        // 1. 检查手机号是否重复
        if (userMapper.existsByPhone(doctorDTO.getPhone())) {
            throw new IllegalArgumentException("手机号已被使用");
        }

        // 2. 插入 user 表（账号信息）
        User user = new User();
        user.setUsername(doctorDTO.getUsername());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD)); // 加密默认密码
        user.setPhone(doctorDTO.getPhone());
        user.setEmail(doctorDTO.getEmail());
        user.setGender(doctorDTO.getGender());
        user.setRoleType("doctor"); // 角色类型为医生
        user.setStatus("verified"); // 状态：已验证
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 3. 插入 doctor 表（医生信息）
        Doctor doctor = new Doctor();
        doctor.setUserId(user.getUserId()); // 绑定 user 表的主键
        doctor.setDeptId(doctorDTO.getDeptId());
        doctor.setTitle(doctorDTO.getTitle());
        doctor.setBio(doctorDTO.getBio());
        doctor.setStatus("active"); // 默认在职
        doctor.setCreatedAt(LocalDateTime.now());
        doctorMapper.insert(doctor);
    }

    /**
     * 修改医生信息（支持修改基础资料和科室信息）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoctor(DoctorAccountDTO doctorDTO) {
        // 1. 查询医生记录
        Doctor doctor = doctorMapper.selectById(doctorDTO.getDoctorId());
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }

        // 2. 更新 user 表中的信息（基础信息）
        User user = userMapper.selectById(doctor.getUserId());
        if (doctorDTO.getUsername() != null) user.setUsername(doctorDTO.getUsername());
        if (doctorDTO.getEmail() != null) user.setEmail(doctorDTO.getEmail());
        if (doctorDTO.getGender() != null) user.setGender(doctorDTO.getGender());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 3. 更新 doctor 表中的专业信息
        if (doctorDTO.getDeptId() != null) doctor.setDeptId(doctorDTO.getDeptId());
        if (doctorDTO.getTitle() != null) doctor.setTitle(doctorDTO.getTitle());
        if (doctorDTO.getBio() != null) doctor.setBio(doctorDTO.getBio());
        if (doctorDTO.getDoctorStatus() != null) doctor.setStatus(doctorDTO.getDoctorStatus());
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorMapper.updateById(doctor);
    }

    /**
     * 修改医生账号状态(启用 / 禁用)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoctorStatus(Long doctorId, String status) {
        // 1. 查询医生
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }

        // 2. 更新 user 表状态(账号层面)
        User user = userMapper.selectById(doctor.getUserId());
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 3. 同步更新医生状态
        if ("rejected".equals(status)) {
            // 禁用账号时,标记医生为退休
            doctor.setStatus("retired");
            doctor.setUpdatedAt(LocalDateTime.now());
            doctorMapper.updateById(doctor);
        } else if ("verified".equals(status)) {
            // 启用账号时,恢复医生为在职状态
            doctor.setStatus("active");
            doctor.setUpdatedAt(LocalDateTime.now());
            doctorMapper.updateById(doctor);
        }
    }

    /**
     * 重置医生密码（管理员可操作）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }

        User user = userMapper.selectById(doctor.getUserId());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD)); // 重置为默认密码
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 根据医生ID查询详细信息
     */
    @Override
    public DoctorAccountDTO getDoctorById(Long doctorId) {
        DoctorAccountDTO doctor = doctorMapper.selectDoctorWithUserById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        return doctor;
    }

    public void submitBioChange(Long doctorId, String newBio) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        // 校验是否有待审核记录
        Long count = requestMapper.selectCount(new QueryWrapper<DoctorBioUpdateRequest>()
                .eq("doctor_id", doctorId)
                .eq("status", "pending")
        );

        if (count > 0) {
            throw new RuntimeException("已有修改申请正在审核中");
        }

        DoctorBioUpdateRequest req = new DoctorBioUpdateRequest();
        req.setDoctorId(doctorId);
        req.setOldBio(doctor.getBio());
        req.setNewBio(newBio);
        req.setStatus("pending");
        req.setCreatedAt(LocalDateTime.now());

        requestMapper.insert(req);
    }

    /**
     * 管理员审核bio修改
     */
    @Transactional
    public void reviewRequest(Long requestId, boolean approved, String reason) {
        DoctorBioUpdateRequest request = requestMapper.selectById(requestId);
        if (request == null || !"pending".equals(request.getStatus())) {
            throw new RuntimeException("无法审核：申请不存在或已处理");
        }

        if (approved) {
            // 修改医生信息
            Doctor doctor = new Doctor();
            doctor.setDoctorId(request.getDoctorId());
            doctor.setBio(request.getNewBio());
            doctor.setUpdatedAt(LocalDateTime.now());
            doctorMapper.updateById(doctor);

            request.setStatus("approved");
            request.setReason(reason);
        } else {
            request.setStatus("rejected");
            request.setReason(reason);
        }

        request.setReviewedAt(LocalDateTime.now());
        requestMapper.updateById(request);
    }

}
