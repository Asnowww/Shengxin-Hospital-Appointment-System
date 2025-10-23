package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.AppointmentCreateParam;
import org.example.backend.dto.AppointmentUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.dto.ScheduleDetailVO;
import org.example.backend.pojo.Appointment;
import org.example.backend.service.AppointmentService;
import org.example.backend.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者端预约控制器
 * 支持查看、创建、取消预约
 */
@RestController
@RequestMapping("/patient/appointment")
public class PatientAppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private ScheduleService scheduleService;

    /**
     * 创建预约（患者挂号）
     *
     * @param param 预约参数（患者ID、排班ID、备注）
     * @return 创建的预约信息
     */
    @PostMapping("/create")
    public Result createAppointment(@RequestBody AppointmentCreateParam param) {
        try {
            // 验证必填参数
            if (param.getPatientId() == null) {
                return Result.error("患者ID不能为空");
            }
            if (param.getScheduleId() == null) {
                return Result.error("排班ID不能为空");
            }

            // 创建预约
            Appointment appointment = appointmentService.createAppointmentByPatient(param);

            return Result.success(appointment);

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 取消预约
     *
     * @param appointmentId 预约ID
     * @param patientId 患者ID（用于权限验证）
     * @param cancelReason 取消原因（可选）
     * @return 取消结果
     */
    @PutMapping("/cancel")
    public Result cancelAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId,
            @RequestParam(required = false) String cancelReason) {
        try {
            // 验证预约是否存在
            Appointment appointment = appointmentService.getById(appointmentId);
            if (appointment == null) {
                return Result.error("预约不存在");
            }

            // 验证权限
            if (!appointment.getPatientId().equals(patientId)) {
                return Result.error("无权取消该预约");
            }

            // 验证状态
            if ("cancelled".equals(appointment.getAppointmentStatus())) {
                return Result.error("预约已取消，请勿重复操作");
            }
            if ("completed".equals(appointment.getAppointmentStatus())) {
                return Result.error("预约已完成，无法取消");
            }

            // 执行取消
            boolean success = appointmentService.cancelAppointment(appointmentId, patientId);


            if (success) {
                return Result.success("预约取消成功");
            } else {
                return Result.error("预约取消失败");
            }

        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 获取当前患者的所有预约
     */
    @GetMapping("/list/{patientId}")
    public Result getAppointmentsByPatient(@PathVariable Long patientId) {
        List<Appointment> list = appointmentService.getAppointmentsByPatientId(patientId);
        return Result.success(list);
    }

    /**
     * 根据日期查询当前患者当天预约
     */
    @GetMapping("/list/{patientId}/date")
    public Result getAppointmentsByDate(@PathVariable Long patientId,
                                        @RequestParam LocalDate date) {
        List<Appointment> list = appointmentService.getAppointmentsByPatientIdAndDate(patientId, date);
        return Result.success(list);
    }

    /**
     * 查询可预约的号源（根据排班）
     * 支持按科室、医生、日期范围筛选
     *
     * @param deptId 科室ID（可选）
     * @param doctorId 医生ID（可选）
     * @param startDate 开始日期（可选，默认当天）
     * @param endDate 结束日期（可选，默认7天后）
     * @param timeSlot 时间段（可选：0-上午，1-下午，2-晚上）
     * @return 可预约的排班列表
     */
    @GetMapping("/available-schedules")
    public Result getAvailableSchedules(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer timeSlot) {

        // 设置默认日期范围：从今天开始到7天后
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(7);
        }

        // 查询所有状态为open且有剩余号源的排班
        List<ScheduleDetailVO> schedules = scheduleService.getAllSchedules(
                deptId, doctorId, startDate, endDate, "open");

        // 过滤：只显示有剩余号源的排班
        List<ScheduleDetailVO> availableSchedules = schedules.stream()
                .filter(schedule -> schedule.getAvailableSlots() > 0)
                .filter(schedule -> {
                    // 如果指定了时间段，进行过滤
                    if (timeSlot != null) {
                        return schedule.getTimeSlot().equals(timeSlot);
                    }
                    return true;
                })
                .toList();

        return Result.success(availableSchedules);
    }

    /**
     * 查询指定科室某一天的可预约号源
     * 常用于患者选择具体就诊日期
     *
     * @param deptId 科室ID
     * @param date 就诊日期
     * @return 当天该科室所有可预约的排班
     */
    @GetMapping("/available-schedules/dept/{deptId}")
    public Result getAvailableSchedulesByDept(
            @PathVariable Integer deptId,
            @RequestParam LocalDate date) {

        List<ScheduleDetailVO> schedules = scheduleService.getDepartmentSchedules(
                deptId, date, date);

        // 过滤：只显示状态为open且有剩余号源的排班
        List<ScheduleDetailVO> availableSchedules = schedules.stream()
                .filter(schedule -> "open".equals(schedule.getStatus()))
                .filter(schedule -> schedule.getAvailableSlots() > 0)
                .toList();

        return Result.success(availableSchedules);
    }

    /**
     * 查询指定医生的可预约号源
     * 常用于患者选择指定医生
     *
     * @param doctorId 医生ID
     * @param startDate 开始日期（可选，默认当天）
     * @param endDate 结束日期（可选，默认14天后）
     * @return 该医生的可预约排班列表
     */
    @GetMapping("/available-schedules/doctor/{doctorId}")
    public Result getAvailableSchedulesByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        // 设置默认日期范围：从今天开始到14天后
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(14);
        }

        List<ScheduleDetailVO> schedules = scheduleService.getDoctorSchedules(
                doctorId, startDate, endDate);

        // 过滤：只显示状态为open且有剩余号源的排班
        List<ScheduleDetailVO> availableSchedules = schedules.stream()
                .filter(schedule -> "open".equals(schedule.getStatus()))
                .filter(schedule -> schedule.getAvailableSlots() > 0)
                .toList();

        return Result.success(availableSchedules);
    }

    /**
     * 修改预约（改期、改时间段）
     *
     * @param param 修改参数（包含预约ID、患者ID、新排班ID等）
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result updateAppointment(@RequestBody AppointmentUpdateParam param) {
        try {
            // 验证必填参数
            if (param.getAppointmentId() == null) {
                return Result.error("预约ID不能为空");
            }
            if (param.getPatientId() == null) {
                return Result.error("患者ID不能为空");
            }

            // 验证是否可以修改
            if (!appointmentService.canUpdateAppointment(
                    param.getAppointmentId(), param.getPatientId())) {
                return Result.error("该预约不允许修改");
            }

            // 执行修改
            boolean success = appointmentService.updateAppointment(param);
            if (success) {
                // 返回更新后的预约信息
                Appointment updatedAppointment = appointmentService.getById(param.getAppointmentId());
                return Result.success(updatedAppointment);
            } else {
                return Result.error("预约修改失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 支付预约
     * 将预约状态从 pending(待支付) 更新为 booked(已预约)
     *
     * @param appointmentId 预约ID
     * @param patientId 患者ID（用于权限验证）
     * @return 支付结果
     */
    @PutMapping("/pay")
    public Result payAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId) {
        try {
            // 验证预约是否存在
            Appointment appointment = appointmentService.getById(appointmentId);
            if (appointment == null) {
                return Result.error("预约不存在");
            }

            // 验证权限
            if (!appointment.getPatientId().equals(patientId)) {
                return Result.error("无权操作该预约");
            }

            // 验证支付状态
            if (!"unpaid".equals(appointment.getPaymentStatus())) {
                return Result.error("该预约不是待支付状态");
            }

            // 验证预约状态
            if (!"pending".equals(appointment.getAppointmentStatus())) {
                return Result.error("该预约状态不允许支付");
            }

            // 验证是否过期
            if (appointment.getExpireTime() != null &&
                    LocalDateTime.now().isAfter(appointment.getExpireTime())) {
                return Result.error("预约已过期，请重新挂号");
            }

            // 执行支付（更新支付状态和预约状态）
            AppointmentUpdateParam param = new AppointmentUpdateParam();
            param.setAppointmentId(appointmentId);
            param.setPatientId(patientId);
            param.setPaymentStatus("paid");
            param.setAppointmentStatus("booked");

            boolean success = appointmentService.updateAppointment(param);

            if (success) {
                Appointment updatedAppointment = appointmentService.getById(appointmentId);
                return Result.success(updatedAppointment);
            } else {
                return Result.error("支付失败");
            }

        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 验证预约是否可以修改
     *
     * @param appointmentId 预约ID
     * @param patientId 患者ID
     * @return 是否可修改
     */
    @GetMapping("/can-update")
    public Result canUpdateAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId) {
        boolean canUpdate = appointmentService.canUpdateAppointment(appointmentId, patientId);
        return Result.success(canUpdate);
    }

    /**
     * 获取预约详情（用于修改前查看）
     *
     * @param appointmentId 预约ID
     * @param patientId 患者ID（用于权限验证）
     * @return 预约详细信息
     */
    @GetMapping("/detail")
    public Result getAppointmentDetail(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId) {
        Appointment appointment = appointmentService.getById(appointmentId);

        if (appointment == null) {
            return Result.error("预约不存在");
        }

        if (!appointment.getPatientId().equals(patientId)) {
            return Result.error("无权查看该预约");
        }

        return Result.success(appointment);
    }

    /**
     * 修改预约备注
     *
     * @param appointmentId 预约ID
     * @param patientId 患者ID
     * @param notes 备注内容
     * @return 修改结果
     */
    @PutMapping("/update-notes")
    public Result updateNotes(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId,
            @RequestParam String notes) {

        AppointmentUpdateParam param = new AppointmentUpdateParam();
        param.setAppointmentId(appointmentId);
        param.setPatientId(patientId);
        param.setNotes(notes);

        try {
            boolean success = appointmentService.updateAppointment(param);
            return success ? Result.success("备注修改成功") : Result.error("备注修改失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


}