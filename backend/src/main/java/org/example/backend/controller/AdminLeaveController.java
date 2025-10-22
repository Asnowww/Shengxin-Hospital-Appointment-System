package org.example.backend.controller;

import org.example.backend.dto.*;
import org.example.backend.pojo.DoctorLeave;
import org.example.backend.pojo.ScheduleException;
import org.example.backend.service.DoctorLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
