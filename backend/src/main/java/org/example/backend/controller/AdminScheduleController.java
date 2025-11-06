package org.example.backend.controller;

import org.example.backend.dto.*;
import org.example.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班管理Controller（管理员端）
 */
@RestController
@RequestMapping("/api/admin/schedules")
@CrossOrigin
public class AdminScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 批量创建排班
     */
    @PostMapping("/batchCreate")
    public Result<Void> createBatchSchedules(@RequestBody ScheduleCreateParam param) {
        try {
            scheduleService.batchCreateSchedules(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建单例排班
     */
    @PostMapping("/create")
    public Result<Void> createSchedules(@RequestBody ScheduleCreateParam param) {
        try {
            scheduleService.createSchedules(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改排班
     */
    @PutMapping("/update")
    public Result<Void> updateSchedule(@RequestBody ScheduleUpdateParam param) {
        try {
            scheduleService.updateSchedule(param);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 停诊（取消排班）
     */
    @DeleteMapping("/{scheduleId}")
    public Result<Void> cancelSchedule(
            @PathVariable Integer scheduleId,
            @RequestParam String reason,
            @RequestParam Long operatorId) {
        try {
            scheduleService.cancelSchedule(scheduleId, reason, operatorId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询所有排班（支持多条件筛选）
     */
    @GetMapping("/list")
    public Result<List<ScheduleDetailVO>> getAllSchedules(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {
        try {
            List<ScheduleDetailVO> schedules = scheduleService.getAllSchedules(deptId, doctorId, startDate, endDate, status);
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询排班详情
     */
    @GetMapping("/{scheduleId}")
    public Result<ScheduleDetailVO> getScheduleDetail(@PathVariable Integer scheduleId) {
        try {
            ScheduleDetailVO schedule = scheduleService.getScheduleDetail(scheduleId);
            if (schedule == null) {
                return Result.error("排班不存在");
            }
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询受影响的挂号记录
     */
    @GetMapping("/{scheduleId}/affected-appointments")
    public Result<List<Long>> getAffectedAppointments(@PathVariable Integer scheduleId) {
        try {
            List<Long> appointmentIds = scheduleService.getAffectedAppointments(scheduleId);
            return Result.success(appointmentIds);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 推荐替代排班
     */
    @GetMapping("/{scheduleId}/alternatives")
    public Result<List<AlternativeScheduleVO>> recommendAlternatives(
            @PathVariable Integer scheduleId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<AlternativeScheduleVO> alternatives = scheduleService.recommendAlternativeSchedules(scheduleId, limit);
            return Result.success(alternatives);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 临时加号
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
