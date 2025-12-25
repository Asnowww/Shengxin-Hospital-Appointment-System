package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.MedicalRecordDTO;
import org.example.backend.dto.MedicalRecordParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.Doctor;
import org.example.backend.pojo.Patient;
import org.example.backend.service.DoctorService;
import org.example.backend.service.MedicalRecordService;
import org.example.backend.service.PatientService;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.baomidou.mybatisplus.extension.ddl.DdlScriptErrorHandler.PrintlnLogErrorHandler.log;

@RestController
@RequestMapping("/api/doctor/medical-record")
public class MedicalRecordController {

    @Resource
    private MedicalRecordService medicalRecordService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private PatientService patientService;

    /**
     * 获取患者最近的病历详情
     */
    @GetMapping("/latest/{patientId}")
    public Result<MedicalRecordDTO> getLatestMedicalRecord(@PathVariable Long patientId) {
        // 查询患者最新病历
        MedicalRecordDTO dto = medicalRecordService.getLatestByPatientId(patientId);
        return Result.success("获取成功", dto);
    }

    /**
     * 获取既往病史这个字段，不是之前的病例
     */
    @GetMapping("/past/{patientId}")
    public Result<String> getPatientMedicalHistory(@PathVariable Long patientId) {
        try {
            Patient patient = patientService.getById(patientId);

            if (patient == null) {
                return Result.error("患者不存在");
            }

            String medicalHistory = patient.getMedicalHistory();

            if (medicalHistory == null || medicalHistory.trim().isEmpty()) {
                medicalHistory = "无既往病史记录";
            }

            return Result.success("获取成功", medicalHistory);

        } catch (Exception e) {
            log.error("获取患者病史失败，patientId: {}");
            return Result.error("获取病史失败");
        }
    }

    /**
     * 创建病历（前端显式调用）
     * 医生ID从Token解析
     */
    @PostMapping("/new")
    public Result<MedicalRecordDTO> createMedicalRecord(
            @RequestBody MedicalRecordParam param,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {

        try {
            // 解析 token 获取 userId
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            // 根据 userId 查医生信息
            Doctor doctor = doctorService.getOne(
                    new QueryWrapper<Doctor>().lambda()
                            .eq(Doctor::getUserId, userId)
            );
            if (doctor == null) {
                return Result.error("医生信息不存在");
            }

            Long doctorId = doctor.getDoctorId();

            // 设置医生ID到 param
            param.setDoctorId(doctorId);

            // 创建病历
            MedicalRecordDTO dto = medicalRecordService.createRecord(param);
            return Result.success("创建成功", dto);

        } catch (Exception e) {
            e.printStackTrace(); // 可以记录日志
            return Result.error("创建病历失败：" + e.getMessage());
        }
    }

    /**
     * 更新病历
     */
    @PutMapping("/{recordId}")
    public Result<MedicalRecordDTO> updateMedicalRecord(@PathVariable Long recordId, @RequestBody MedicalRecordParam param) {
        MedicalRecordDTO dto = medicalRecordService.updateRecord(recordId, param);
        if (dto == null) return Result.error("病历不存在");
        return Result.success("更新成功", dto);
    }

    /**
     * 获取患者历史病历
     */
    @GetMapping("/history/{patientId}")
    public Result<List<MedicalRecordDTO>> getPatientHistory(@PathVariable Long patientId,
                                                            @RequestParam(defaultValue = "50") Integer limit) {
        List<MedicalRecordDTO> list = medicalRecordService.getHistoryByPatientId(patientId, limit);
        return Result.success("获取成功", list);
    }
}
