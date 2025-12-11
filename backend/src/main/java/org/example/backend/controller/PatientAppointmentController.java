package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.*;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.Doctor;
import org.example.backend.pojo.Patient;
import org.example.backend.service.AppointmentService;
import org.example.backend.service.DoctorService;
import org.example.backend.service.PatientService;
import org.example.backend.service.ScheduleService;
import org.example.backend.service.AuditLogService;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;
import org.example.backend.dto.AppointmentInfoDTO;

import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 患者端预约控制器
 * 支持查看、创建、取消预约、改约
 */
@RestController
@RequestMapping("/api/patient/appointment")
public class PatientAppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private DoctorService doctorService;

    /**
     * 创建预约（患者挂号）
     *
     */
    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private PatientService patientService;

    @Resource
    private AuditLogService auditLogService;

    @PostMapping("/create")
    public Result<Appointment> createAppointment(
            @RequestBody AppointmentCreateParam param,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
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
                                .eq(Patient::getUserId, userId));
                if (patient == null) {
                    return Result.error("患者信息不存在");
                }
                param.setPatientId(patient.getPatientId());
            }

            // 验证必填参数
            if (param.getScheduleId() == null) {
                return Result.error("排班ID不能为空");
            }

            // 创建预约
            Appointment appointment = appointmentService.createAppointmentByPatient(param);

            recordAudit(userId, "create", "appointment",
                    appointment.getAppointmentId(),
                    "患者创建预约 scheduleId=" + param.getScheduleId(),
                    request);

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
     * @param cancelReason  取消原因（可选）
     * @return 取消结果
     */
    @PutMapping("/cancel")
    public Result<String> cancelAppointment(
            @RequestParam Long appointmentId,
            @RequestParam(required = false) String cancelReason,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
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

            // 根据 userId 查 patientId
            Patient patient = patientService.getOne(
                    new QueryWrapper<Patient>().lambda()
                            .eq(Patient::getUserId, userId));
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            Long patientId = patient.getPatientId();

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
                recordAudit(userId, "delete", "appointment",
                        appointmentId,
                        "患者取消预约 reason=" + cancelReason,
                        request);
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
    @GetMapping("/list")
    public Result<List<AppointmentInfoDTO>> getAppointmentsByPatient(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {
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

            // 根据 userId 查 patientId
            Patient patient = patientService.getOne(
                    new QueryWrapper<Patient>().lambda()
                            .eq(Patient::getUserId, userId));
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            // 查询该患者的预约
            List<AppointmentInfoDTO> list = appointmentService.getAppointmentsByPatientId(patient.getPatientId());

            return Result.success(list);

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 根据日期查询当前患者当天预约
     */
    @GetMapping("/list/{patientId}/date")
    public Result<List<Appointment>> getAppointmentsByDate(@PathVariable Long patientId,
            @RequestParam LocalDate date) {
        List<Appointment> list = appointmentService.getAppointmentsByPatientIdAndDate(patientId, date);
        return Result.success(list);
    }

    /**
     * 查询可预约的号源（根据排班）
     * 支持按科室、医生、日期范围筛选
     *
     * @param deptId    科室ID（可选）
     * @param doctorId  医生ID（可选）
     * @param startDate 开始日期（可选，默认当天）
     * @param endDate   结束日期（可选，默认7天后）
     * @param timeSlot  时间段（可选：0-上午，1-下午，2-晚上）
     * @return 可预约的排班列表
     */
    @GetMapping("/available-schedules")
    public Result<List<ScheduleDetailVO>> getAvailableSchedules(
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

        if (doctorId != null) {
            Doctor doctor = doctorService.getById(doctorId); // 或者 doctorMapper.selectById(doctorId)
            if (doctor == null) {
                return Result.error("医生不存在，doctorId=" + doctorId);
            }
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
     * @param date   就诊日期
     * @return 当天该科室所有可预约的排班
     */
    @GetMapping("/available-schedules/dept/{deptId}")
    public Result<List<ScheduleDetailVO>> getAvailableSchedulesByDept(
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
     * 查询指定医生全部号源
     */
    @GetMapping("/all-schedules/doctor/{doctorId}")
    public Result<List<ScheduleDetailVO>> getAllSchedulesByDoctor(
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

        List<ScheduleDetailVO> schedules = scheduleService.getDoctorSchedulesByDoctorId(
                doctorId, startDate, endDate);

        return Result.success(schedules);
    }

    /**
     * 查询指定医生的可预约号源
     * 常用于患者选择指定医生
     *
     * @param doctorId  医生ID
     * @param startDate 开始日期（可选，默认当天）
     * @param endDate   结束日期（可选，默认14天后）
     * @return 该医生的可预约排班列表
     */
    @GetMapping("/available-schedules/doctor/{doctorId}")
    public Result<List<ScheduleDetailVO>> getAvailableSchedulesByDoctor(
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
    public Result<Appointment> updateAppointment(@RequestBody AppointmentUpdateParam param) {
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

                // 无 token，这里直接用传入 patientId 作为 userId 记录
                recordAudit(param.getPatientId(), "update", "appointment",
                        param.getAppointmentId(),
                        "患者修改预约 newScheduleId=" + param.getNewScheduleId(),
                        null);
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
     * 支付预约（修改后的版本）
     * 调用 AppointmentServiceImpl 中的 payAppointment 方法
     *
     * @param appointmentId 预约ID
     * @param paymentMethod 支付方式（alipay/wechat/card/cash）
     * @return 支付结果
     */
    @PutMapping("/pay")
    public Result<Appointment> payAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String paymentMethod,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
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

            // 根据 userId 查 patientId
            Patient patient = patientService.getOne(
                    new QueryWrapper<Patient>().lambda()
                            .eq(Patient::getUserId, userId));
            if (patient == null) {
                return Result.error("患者信息不存在");
            }

            Long patientId = patient.getPatientId();

            // 验证支付方式
            if (!paymentMethod.matches("alipay|wechat|card")) {
                return Result.error("不支持的支付方式，请选择: alipay, wechat, card ");
            }

            // 调用支付服务
            boolean success = appointmentService.payAppointment(appointmentId, patientId, paymentMethod);

            if (success) {
                // 返回更新后的预约信息
                Appointment updatedAppointment = appointmentService.getById(appointmentId);

                recordAudit(userId, "update", "payment",
                        appointmentId,
                        "预约支付方式=" + paymentMethod,
                        request);
                return Result.success(updatedAppointment);
            } else {
                return Result.error("支付失败");
            }

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 验证预约是否可以修改
     *
     * @param appointmentId 预约ID
     * @param patientId     患者ID
     * @return 是否可修改
     */
    @GetMapping("/can-update")
    public Result<Boolean> canUpdateAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId) {
        boolean canUpdate = appointmentService.canUpdateAppointment(appointmentId, patientId);
        return Result.success(canUpdate);
    }

    /**
     * 获取预约详情（用于修改前查看）
     *
     * @param appointmentId 预约ID
     * @param patientId     患者ID（用于权限验证）
     * @return 预约详细信息
     */
    @GetMapping("/detail")
    public Result<Appointment> getAppointmentDetail(
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
     * @param patientId     患者ID
     * @param notes         备注内容
     * @return 修改结果
     */
    @PutMapping("/update-notes")
    public Result<String> updateNotes(
            @RequestParam Long appointmentId,
            @RequestParam Long patientId,
            @RequestParam String notes,
            HttpServletRequest request) {

        AppointmentUpdateParam param = new AppointmentUpdateParam();
        param.setAppointmentId(appointmentId);
        param.setPatientId(patientId);
        param.setNotes(notes);

        try {
            boolean success = appointmentService.updateAppointment(param);
            if (success) {
                recordAudit(patientId, "update", "appointment",
                        appointmentId,
                        "患者修改备注",
                        request);
                return Result.success("备注修改成功");
            }
            return Result.error("备注修改失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private void recordAudit(Long userId, String action, String resourceType,
            Long resourceId, String message, HttpServletRequest request) {
        try {
            AuditLogCreateDTO dto = new AuditLogCreateDTO();
            dto.setAction(action);
            dto.setResourceType(resourceType);
            dto.setResourceId(resourceId);
            dto.setMessage(message);
            dto.setIp(request != null ? request.getRemoteAddr() : null);
            auditLogService.recordLog(userId, dto);
        } catch (Exception e) {
            // 审计失败不影响主流程
        }
    }

}