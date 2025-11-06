package org.example.backend.controller;

import org.example.backend.dto.Result;
import org.example.backend.dto.ScheduleDetailVO;
import org.example.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者查看排班Controller（患者端）
 */
@RestController
@RequestMapping("/api/patient/schedules")
@CrossOrigin
public class PatientScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 查询可预约的排班（按科室）
     */
    @GetMapping("/available")
    public Result<List<ScheduleDetailVO>> getAvailableSchedules(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // 只查询状态为 open 且有剩余号源的排班
            List<ScheduleDetailVO> schedules = scheduleService.getAllSchedules(deptId, doctorId, startDate, endDate, "open");

            // 过滤掉没有剩余号源的排班
            schedules = schedules.stream()
                    .filter(s -> s.getAvailableSlots() > 0)
                    .collect(java.util.stream.Collectors.toList());

            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询所有排班
     */
    @GetMapping("/all")
    public Result<List<ScheduleDetailVO>> getAllSchedules(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // 查询所有状态的排班
            List<ScheduleDetailVO> schedules = scheduleService.getAllSchedules(deptId, doctorId, startDate, endDate, null);

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
}
