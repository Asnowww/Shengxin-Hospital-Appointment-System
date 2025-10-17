package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.PatientUpdateParam;
import org.example.backend.mapper.PatientMapper;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.PatientService;
import org.example.backend.service.UserService;
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
        return this.getOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, userId));
    }

    @Override
    @Transactional
    public boolean updatePatientInfo(Long userId, PatientUpdateParam param) {
        // 1. 验证用户是否存在且为患者角色
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!"patient".equals(user.getRoleType())) {
            throw new RuntimeException("该用户不是患者角色");
        }

        // 2. 检查唯一性约束（邮箱、手机号）
        if (param.getEmail() != null && !param.getEmail().equals(user.getEmail())) {
            User existUser = userService.findByEmail(param.getEmail());
            if (existUser != null && !existUser.getUserId().equals(userId)) {
                throw new RuntimeException("该邮箱已被其他用户使用");
            }
        }

        if (param.getPhone() != null && !param.getPhone().equals(user.getPhone())) {
            User existUser = userService.findByPhone(param.getPhone());
            if (existUser != null && !existUser.getUserId().equals(userId)) {
                throw new RuntimeException("该手机号已被其他用户使用");
            }
        }

        // 3. 更新用户基础信息
        if (param.getGender() != null)
            user.setGender(param.getGender());
        if (param.getPhone() != null)
            user.setPhone(param.getPhone());
        if (param.getEmail() != null)
            user.setEmail(param.getEmail());

        boolean userUpdated = userService.updateById(user);
        if (!userUpdated) {
            throw new RuntimeException("更新用户信息失败");
        }

        // 4. 更新患者详细信息
        Patient patient = getPatientByUserId(userId);
        if (patient == null) {
            throw new RuntimeException("患者信息不存在");
        }

        // 只更新非空字段
        if (param.getPatientAccount() != null)
            patient.setPatientAccount(param.getPatientAccount());
        if (param.getIdentityType() != null)
            patient.setIdentityType(param.getIdentityType());
        if (param.getBirthDate() != null)
            patient.setBirthDate(param.getBirthDate());
        if (param.getAddress() != null)
            patient.setAddress(param.getAddress());
        if (param.getHeight() != null)
            patient.setHeight(param.getHeight());
        if (param.getWeight() != null)
            patient.setWeight(param.getWeight());
        if (param.getMedicalHistory() != null)
            patient.setMedicalHistory(param.getMedicalHistory());
        if (param.getEmergencyContact() != null)
            patient.setEmergencyContact(param.getEmergencyContact());
        if (param.getEmergencyPhone() != null)
            patient.setEmergencyPhone(param.getEmergencyPhone());

        int patientUpdated = this.updateById(patient) ? 1 : 0;
        if (patientUpdated <= 0) {
            throw new RuntimeException("更新患者信息失败");
        }

        return true;
    }
}
