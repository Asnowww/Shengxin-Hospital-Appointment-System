package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.PatientUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.User;
import org.example.backend.service.PatientService;
import org.example.backend.service.UserService;
import org.example.backend.util.TokenUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")

public class PatientController {
    @Resource
    private UserService userService;

    @Resource
    private PatientService patientService;

    @Resource
    private TokenUtil tokenUtil; // 注入工具类

    /**
     * 使用 token 获取患者个人信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getPatientProfileByToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam) {

        String token = tokenUtil.extractToken(authorization, tokenParam);
        Long userId = tokenUtil.resolveUserIdFromToken(token);
        if (userId == null) {
            return new Result<>(401, "未登录或token无效", null);
        }

        try {
            User user = userService.getById(userId);
            if (user == null)
                return new Result<>(404, "用户不存在", null);
            if (!"patient".equals(user.getRoleType()))
                return new Result<>(403, "该用户不是患者角色", null);

            var patient = patientService.getPatientByUserId(userId);
            if (patient == null)
                return new Result<>(404, "患者信息不存在", null);

            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("gender", user.getGender());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());
            data.put("roleType", user.getRoleType());
            data.put("status", user.getStatus());
            data.put("createTime", user.getCreateTime());
            data.put("updateTime", user.getUpdateTime());

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
     * 使用 token 更新患者个人信息
     */
    @PutMapping("/profile/update")
    public Result<Void> updatePatientProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestBody PatientUpdateParam param) {

        // 提取 token 并解析 userId
        String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
        Long userId = tokenUtil.resolveUserIdFromToken(token);
        if (userId == null) {
            return new Result<>(401, "无效的登录凭证", null);
        }

        //更新患者信息
        try {
            // 调用 service 更新信息
            return patientService.updatePatientInfo(userId, param);
        } catch (IllegalArgumentException e) {
            return new Result<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new Result<>(500, "更新患者信息失败：" + e.getMessage(), null);
        }
    }
}
