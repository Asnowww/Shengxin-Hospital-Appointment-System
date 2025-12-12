package org.example.backend.controller;

import org.example.backend.dto.*;
import org.example.backend.pojo.DoctorLeave;
import org.example.backend.service.DoctorLeaveService;
import org.example.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生排班Controller（医生端）
 */
@RestController
@RequestMapping("/api/doctor/schedules")
@CrossOrigin
public class DoctorScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DoctorLeaveService doctorLeaveService;

    /**
     * 查看我的排班
     */
    @GetMapping("/my")
    public Result<List<ScheduleDetailVO>> getMySchedules(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<ScheduleDetailVO> schedules = scheduleService.getDoctorSchedules(userId, startDate, endDate);
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 申请请假
     */
    @PostMapping("/leave/apply")
    public Result<Void> applyLeave(@RequestBody LeaveApplyParam param) {
        try {
            doctorLeaveService.applyLeave(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    /**
     * 申请调班
     */
    @PostMapping("/adjust/apply")
    public Result<Void> applyAdjustment(@RequestBody ScheduleAdjustParam param) {
        try {
            doctorLeaveService.applyScheduleAdjust(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看我的请假历史
     */
    @GetMapping("/leave/history")
    public Result<List<DoctorLeave>> getLeaveHistory(@RequestParam Long userId) {
        try {
            List<DoctorLeave> leaves = doctorLeaveService.getDoctorLeaveHistory(userId);
            return Result.success(leaves);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 临时加号（医生端也可以操作）
     */
    @PostMapping("/add-extra-slots")
    public Result<Void> addExtraSlots(@RequestBody AddExtraSlotsParam param) {
        try {
            scheduleService.addExtraSlots(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
