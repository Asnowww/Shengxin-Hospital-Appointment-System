package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.PatientUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.service.UserService;
import org.example.backend.mapper.PatientMapper;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.PatientService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * 患者业务逻辑实现类
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Resource
    private UserService userService;

    @Override
    public Patient getPatientByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, userId));
    }

    //更新患者个人信息
    @Override
    @Transactional
    public Result<Void> updatePatientInfo(Long userId, PatientUpdateParam param) {
        // 1. 验证用户是否存在且为患者角色
        User user = userService.getById(userId);
        if (user == null) {
            return new Result<>(404, "用户不存在", null);
        }
        if (!"patient".equals(user.getRoleType())) {
            return new Result<>(400, "该用户不是患者角色", null);
        }

        // 2. 检查唯一性约束（邮箱、手机号）
        if (param.getEmail() != null && !param.getEmail().equals(user.getEmail())) {
            User existUser = userService.findByEmail(param.getEmail());
            if (existUser != null && !existUser.getUserId().equals(userId)) {
                return new Result<>(400, "该邮箱已被其他用户使用", null);
            }
        }

        if (param.getPhone() != null && !param.getPhone().equals(user.getPhone())) {
            User existUser = userService.findByPhone(param.getPhone());
            if (existUser != null && !existUser.getUserId().equals(userId)) {
                return new Result<>(400, "该手机号已被其他用户使用", null);
            }
        }

        // 3. 更新用户基础信息
        if (param.getGender() != null) user.setGender(param.getGender());
        if (param.getPhone() != null) user.setPhone(param.getPhone());
        if (param.getEmail() != null) user.setEmail(param.getEmail());

        boolean userUpdated = userService.updateById(user);
        if (!userUpdated) {
            return new Result<>(500, "更新用户信息失败", null);
        }

        // 4. 更新患者详细信息
        Patient patient = getPatientByUserId(userId);
        if (patient == null) {
            return new Result<>(404, "患者信息不存在", null);
        }

        if (param.getPatientAccount() != null) patient.setPatientAccount(param.getPatientAccount());
        if (param.getIdentityType() != null) patient.setIdentityType(param.getIdentityType());
        if (param.getBirthDate() != null) patient.setBirthDate(param.getBirthDate());
        if (param.getAddress() != null) patient.setAddress(param.getAddress());
        if (param.getHeight() != null) patient.setHeight(param.getHeight());
        if (param.getWeight() != null) patient.setWeight(param.getWeight());
        if (param.getMedicalHistory() != null) patient.setMedicalHistory(param.getMedicalHistory());
        if (param.getEmergencyContact() != null) patient.setEmergencyContact(param.getEmergencyContact());
        if (param.getEmergencyPhone() != null) patient.setEmergencyPhone(param.getEmergencyPhone());

        boolean patientUpdated = this.updateById(patient);
        if (!patientUpdated) {
            return new Result<>(500, "更新患者信息失败", null);
        }

        // 5. 返回成功结果
        return new Result<>(200, "更新成功", null);
    }

    @Resource
    private PatientMapper patientMapper;

    @Override
    public Long getPatientIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        // 查询 patient 表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Patient::getUserId, userId);

        Patient patient = patientMapper.selectOne(wrapper);
        if (patient != null) {
            return patient.getPatientId();
        } else {
            return null;
        }
    }
}
