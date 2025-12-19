package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.MedicalRecordDTO;
import org.example.backend.dto.Result;
import org.example.backend.service.MedicalRecordService;
import org.example.backend.service.PatientService;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专门供患者访问的病历控制器
 */
@RestController
@RequestMapping("/api/patient/medical-record")
public class PatientMedicalRecordController {

    @Resource
    private MedicalRecordService medicalRecordService;

    @Resource
    private PatientService patientService;

    @Resource
    private TokenUtil tokenUtil;

    /**
     * 患者获取自己的历史病历
     */
    @GetMapping("/my-history")
    public Result<List<MedicalRecordDTO>> getMyMedicalHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam(defaultValue = "100") Integer limit) {

        try {
            // 1. 从 Token 中解析 userId
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error(401, "未登录或登录已过期");
            }

            // 2. 根据 userId 获取 patientId
            Long patientId = patientService.getPatientIdByUserId(userId);
            if (patientId == null) {
                return Result.error(404, "患者信息不存在");
            }

            // 3. 查询此患者的历史病历
            List<MedicalRecordDTO> list = medicalRecordService.getHistoryByPatientId(patientId, limit);
            return Result.success("获取历史病历成功", list);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取病历失败：" + e.getMessage());
        }
    }
}
