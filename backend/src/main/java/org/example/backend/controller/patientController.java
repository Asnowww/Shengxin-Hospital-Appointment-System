package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.PatientUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.PatientService;
import org.example.backend.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class patientController {
    @Resource
    private UserService userService;

    @Resource
    private PatientService patientService;

    /**
     * 获取患者个人信息
     */
    @GetMapping("/patient/profile/{userId}")
    @ResponseBody
    public Result<Map<String, Object>> getPatientProfile(@PathVariable Long userId) {
        try {
            // 获取用户基础信息
            User user = userService.getById(userId);
            if (user == null) {
                return new Result<>(404, "用户不存在", null);
            }

            if (!"patient".equals(user.getRoleType())) {
                return new Result<>(403, "该用户不是患者角色", null);
            }

            // 获取患者详细信息
            Patient patient = patientService.getPatientByUserId(userId);
            if (patient == null) {
                return new Result<>(404, "患者信息不存在", null);
            }

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();

            // 用户基础信息
            data.put("userId", user.getUserId());
            data.put("username", user.getUsername());
            data.put("gender", user.getGender());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());
            data.put("roleType", user.getRoleType());
            data.put("status", user.getStatus());
            data.put("createTime", user.getCreateTime());
            data.put("updateTime", user.getUpdateTime());

            // 患者详细信息
            data.put("patientId", patient.getPatientId());
            data.put("patientAccount", patient.getPatientAccount());
            data.put("identityType", patient.getIdentityType());
            data.put("birthDate", patient.getBirthDate());
            data.put("address", patient.getAddress());
            data.put("height", patient.getHeight());
            data.put("weight", patient.getWeight());
            data.put("medicalHistory", patient.getMedicalHistory());
            data.put("emergencyContact", patient.getEmergencyContact());
            data.put("emergencyPhone", patient.getEmergencyPhone());

            return new Result<>(200, "获取患者信息成功", data);
        } catch (Exception e) {
            return new Result<>(500, "获取患者信息失败：" + e.getMessage(), null);
        }
    }

    /**
     * 更新患者个人信息
     */
    @PutMapping("/patient/profile/{userId}")
    @ResponseBody
    public Result<Void> updatePatientProfile(@PathVariable Long userId, @RequestBody PatientUpdateParam param) {
        try {
            boolean success = patientService.updatePatientInfo(userId, param);
            if (success) {
                return new Result<>(200, "更新患者信息成功", null);
            } else {
                return new Result<>(400, "更新患者信息失败", null);
            }
        } catch (RuntimeException e) {
            return new Result<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new Result<>(500, "更新患者信息失败：" + e.getMessage(), null);
        }
    }

}
