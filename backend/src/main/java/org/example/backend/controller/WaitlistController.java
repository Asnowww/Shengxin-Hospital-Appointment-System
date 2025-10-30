package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.Data;
import org.example.backend.dto.ScheduleDetailVO;
import org.example.backend.dto.WaitlistCreateParam;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.Waitlist;
import org.example.backend.service.PatientService;
import org.example.backend.service.ScheduleService;
import org.example.backend.service.WaitlistService;
import org.example.backend.dto.Result;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/waitlist")
public class WaitlistController {

    @Resource
    private WaitlistService waitlistService;

    @Resource
    private PatientService patientService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private org.example.backend.mapper.WaitlistMapper waitlistMapper;

    /**
     * 创建候补预约
     * POST /waitlist/create
     */
    @PostMapping("/create")
    public Result<Waitlist> createWaitlist(
            @RequestBody WaitlistCreateParam param,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
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

            // 如果 param 中没有 patientId，则根据 userId 查询 patientId
            if (param.getPatientId() == null) {
                Patient patient = patientService.getOne(
                        new QueryWrapper<Patient>().lambda()
                                .eq(Patient::getUserId, userId)
                );
                if (patient == null) {
                    return Result.error("患者信息不存在");
                }
                param.setPatientId(String.valueOf(patient.getPatientId()));
            }

            // 创建候补
            Waitlist waitlist = waitlistService.createWaitlist(param);
            return Result.success("候补成功，有号源时将自动为您预约", waitlist);

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("候补失败：" + e.getMessage());
        }
    }

    /**
     * 取消候补
     * POST /waitlist/cancel/{waitId}
     */
    @PostMapping("/cancel/{waitId}")
    public Result<Void> cancelWaitlist(
            @PathVariable Long waitId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            // 提取并验证 token
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            if (token == null) {
                return Result.error("未提供 token");
            }

            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            // 获取 patientId
            Patient patient = patientService.getOne(
                    new QueryWrapper<Patient>().lambda()
                            .eq(Patient::getUserId, userId)
            );
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            // 取消候补
            boolean success = waitlistService.cancelWaitlist(waitId, patient.getPatientId());
            if (success) {
                return Result.success("候补已取消", null);
            } else {
                return Result.error("取消失败，候补不存在或无权操作");
            }

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消候补失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的候补列表
     * GET /waitlist/my
     */
    @GetMapping("/my")
    public Result<List<Waitlist>> getMyWaitlists(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            // 提取并验证 token
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            if (token == null) {
                return Result.error("未提供 token");
            }

            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            // 获取 patientId
            Patient patient = patientService.getOne(
                    new QueryWrapper<Patient>().lambda()
                            .eq(Patient::getUserId, userId)
            );
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            // 查询候补列表
            List<Waitlist> waitlists = waitlistService.getPatientWaitlists(patient.getPatientId());
            return Result.success(waitlists);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 检查某个排班的候补情况（供前端展示）
     * GET /waitlist/check/{scheduleId}
     * 返回候补统计信息，包括总人数和高优先级人数
     */
    @GetMapping("/check/{scheduleId}")
    public Result<WaitlistStatistics> checkWaitlist(@PathVariable Integer scheduleId) {
        try {
            boolean hasWaitlist = waitlistService.hasWaitlist(scheduleId);

            WaitlistStatistics statistics = new WaitlistStatistics();
            statistics.setHasWaitlist(hasWaitlist);

            if (hasWaitlist) {
                // 查询候补队列详情
                QueryWrapper<Waitlist> wrapper = new QueryWrapper<>();
                wrapper.eq("schedule_id", scheduleId)
                        .eq("status", "waiting");

                List<Waitlist> waitlists = waitlistMapper.selectList(wrapper);

                statistics.setTotalWaiting(waitlists.size());
                statistics.setHighPriorityCount(
                        (int) waitlists.stream().filter(w -> w.getPriority() >= 1).count()
                );
            } else {
                statistics.setTotalWaiting(0);
                statistics.setHighPriorityCount(0);
            }

            return Result.success(statistics);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询已预约满的排班（可供候补）
     * 支持按科室、医生、日期范围筛选
     *
     * @param deptId 科室ID（可选）
     * @param doctorId 医生ID（可选）
     * @param startDate 开始日期（可选，默认当天）
     * @param endDate 结束日期（可选，默认7天后）
     * @param timeSlot 时间段（可选：0-上午，1-下午，2-晚上）
     * @return 已满排班列表及候补情况
     */
    @GetMapping("/fully-booked-schedules")
    public Result<List<ScheduleDetailVO>> getFullyBookedSchedules(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer timeSlot) {

        // 默认日期范围：今天到7天后
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }

        // 查询状态为open且已满的排班
        List<ScheduleDetailVO> schedules = scheduleService.getAllSchedules(
                deptId, doctorId, startDate, endDate, "open");

        List<ScheduleDetailVO> fullyBooked = schedules.stream()
                .filter(schedule -> schedule.getAvailableSlots() == 0)
                .filter(schedule -> timeSlot == null || schedule.getTimeSlot().equals(timeSlot))
                .map(schedule -> {
                    ScheduleDetailVO vo = new ScheduleDetailVO();
                    vo.setScheduleId(schedule.getScheduleId());
                    vo.setDoctorId(schedule.getDoctorId());
                    vo.setDoctorName(schedule.getDoctorName());
                    vo.setDoctorTitle(schedule.getDoctorTitle());
                    vo.setDeptId(schedule.getDeptId());
                    vo.setDeptName(schedule.getDeptName());
                    vo.setWorkDate(schedule.getWorkDate());
                    vo.setTimeSlot(schedule.getTimeSlot());
                    vo.setTimeSlotName(schedule.getTimeSlotName());
                    vo.setMaxSlots(schedule.getMaxSlots());
                    vo.setBookedSlots(schedule.getBookedSlots());

                    return vo;
                })
                .toList();

        return Result.success(fullyBooked);
    }


    /**
     * 候补统计信息 VO
     */
    @Data
    public static class WaitlistStatistics {
        private Boolean hasWaitlist;        // 是否有候补
        private Integer totalWaiting;       // 总候补人数
        private Integer highPriorityCount;  // 高优先级（>=1）候补人数
    }
}