package org.example.backend.controller;

import org.example.backend.dto.*;
import org.example.backend.pojo.DoctorLeave;
import org.example.backend.pojo.ScheduleException;
import org.example.backend.service.DoctorLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 医生请假/调班管理Controller（管理员端）
 */
@RestController
@RequestMapping("/api/admin/leaves")
@CrossOrigin
public class AdminLeaveController {

    @Autowired
    private DoctorLeaveService doctorLeaveService;

    /**
     * 查询请假申请列表
     */
    @GetMapping("/list")
    public Result<List<DoctorLeaveVO>> getLeaveApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long doctorId) {
        try {
            List<DoctorLeaveVO> leaves = doctorLeaveService.getLeaveApplications(status, doctorId);
            return Result.success(leaves);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批请假申请
     */
    @PutMapping("/review")
    public Result<Void> reviewLeave(@RequestBody LeaveReviewParam param) {
        try {
            doctorLeaveService.reviewLeave(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询调班申请列表
     */
    @GetMapping("/adjustments/list")
    public Result<List<ScheduleException>> getAdjustApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long doctorId) {
        try {
            List<ScheduleException> adjustments = doctorLeaveService.getAdjustApplications(status, doctorId);
            return Result.success(adjustments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批调班申请
     */
    @PutMapping("/adjustments/{exceptionId}/review")
    public Result<Void> reviewAdjustment(
            @PathVariable Long exceptionId,
            @RequestParam String action,
            @RequestParam Long reviewedBy) {
        try {
            doctorLeaveService.reviewScheduleAdjust(exceptionId, action, reviewedBy);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员取消排班的临时处理
     * @param payload
     * @return
     */
    @PostMapping("/cancel")
    public Result<String> cancelSchedule(@RequestBody Map<String, Object> payload) {
        // 1. 安全提取并校验参数
        Object sId = payload.get("scheduleId");
        Object oId = payload.get("operatorId");
        String reason = (String) payload.getOrDefault("reason", "管理员操作");

        if (sId == null || oId == null) {
            return Result.error("缺少必要参数：scheduleId 或 operatorId");
        }

        try {
            // 2. 健壮的类型转换：先转 String 再解析，可同时兼容 JSON 中的数字和字符串
            Integer scheduleId = Integer.valueOf(sId.toString());
            Long operatorId = Long.valueOf(oId.toString());

            // 3. 执行业务逻辑
            doctorLeaveService.handleCancelSchedule(scheduleId, operatorId, reason);

            return Result.success("排班已取消，相关预约处理完毕");
        } catch (NumberFormatException e) {
            return Result.error("参数格式错误：ID必须为数字");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("服务器内部错误");
        }
    }
}

