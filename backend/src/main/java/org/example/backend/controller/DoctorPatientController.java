package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.AppointmentInfoDTO;
import org.example.backend.dto.DoctorSchedulePatientsVO;
import org.example.backend.dto.Result;
import org.example.backend.dto.SchedulePatientVO;
import org.example.backend.pojo.Doctor;
import org.example.backend.service.AppointmentService;
import org.example.backend.service.DoctorService;
import org.example.backend.service.ScheduleService;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/patient")
public class DoctorPatientController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private TokenUtil tokenUtil;

    /**
     * 获取医生指定日期的所有排班患者列表
     */
    @GetMapping("/schedules-line")
    public Result<List<DoctorSchedulePatientsVO>> getDoctorSchedulePatients(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam(value = "date", required = false) String dateStr) {
        try {
            // 提取 token
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            if (token == null) {
                return Result.error("未提供 token");
            }

            // 解析 userId
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            // 根据 userId 查询 doctorId
            Doctor doctor = doctorService.getOne(
                    new QueryWrapper<Doctor>().lambda()
                            .eq(Doctor::getUserId, userId)
            );
            if (doctor == null) {
                return Result.error("医生信息不存在");
            }

            // 解析日期，如果没有传日期则默认今天
            LocalDate date = (dateStr != null && !dateStr.isEmpty())
                    ? LocalDate.parse(dateStr)
                    : LocalDate.now();

            // 查询医生指定日期的所有排班患者
            List<DoctorSchedulePatientsVO> result = scheduleService.getDoctorSchedulePatientsGrouped(
                    doctor.getDoctorId(),
                    date
            );

            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记患者已就诊
     */
    @PutMapping("/{appointmentId}/completed")
    public Result<Object> markPatientCompleted(@PathVariable Long appointmentId) {
        try {
            boolean success = appointmentService.appointmentComplete(appointmentId);
            if (success) {
                return Result.success("已标记为已就诊");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 叫号
     */
    @PostMapping("/{appointmentId}/call")
    public Result<Object> callPatient(@PathVariable Long appointmentId) {
        try {
            boolean success = appointmentService.callPatient(appointmentId);
            if (success) {
                return Result.success("叫号成功，已发送通知");
            } else {
                return Result.error("叫号失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    /**
     * 获取患者的所有历史就诊记录
     */
    @GetMapping("/{patientId}/history")
    public Result<List<AppointmentInfoDTO>> getPatientHistory(
            @PathVariable Long patientId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        try {
            List<AppointmentInfoDTO> history = appointmentService.getPatientHistory(patientId, limit);
            return Result.success("获取成功", history);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}