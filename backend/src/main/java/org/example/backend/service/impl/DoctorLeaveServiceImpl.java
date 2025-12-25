package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.*;
import org.example.backend.mapper.*;
import org.example.backend.pojo.*;
import org.example.backend.service.AppointmentService;
import org.example.backend.service.DoctorLeaveService;
import org.example.backend.service.NotificationEmailService;
import org.example.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorLeaveServiceImpl implements DoctorLeaveService {

    @Autowired
    private DoctorLeaveMapper doctorLeaveMapper;

    @Autowired
    private ScheduleExceptionMapper scheduleExceptionMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AppointmentRelationsMapper appointmentRelationsMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;


    @Autowired
    private ScheduleService scheduleService;

    @Resource
    private NotificationEmailService notificationEmailService;
    @Resource
    private AppointmentService appointmentService;

    @Override
    @Transactional
    public void applyLeave(LeaveApplyParam param) {
        if (param.getScheduleIds() == null || param.getScheduleIds().isEmpty()) {
            throw new RuntimeException("请选择至少一个需要请假的排班");
        }

        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, param.getUserId()));
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }
        Long doctorId = doctor.getDoctorId();

        QueryWrapper<Schedule> scheduleQuery = new QueryWrapper<>();
        scheduleQuery.in("schedule_id", param.getScheduleIds());
        List<Schedule> targetSchedules = scheduleMapper.selectList(scheduleQuery);
        if (targetSchedules.size() != param.getScheduleIds().size()) {
            throw new RuntimeException("存在无效的排班记录，请刷新后重试");
        }

        LocalDate today = LocalDate.now();
        for (Schedule schedule : targetSchedules) {
            if (!doctorId.equals(schedule.getDoctorId())) {
                throw new RuntimeException("排班不属于当前医生，无法申请请假");
            }
            if ("cancelled".equals(schedule.getStatus())) {
                throw new RuntimeException("已取消的排班无需请假");
            }
            if (schedule.getWorkDate().isBefore(today)) {
                throw new RuntimeException("不能对历史排班发起请假申请");
            }
        }

        // 检查是否存在针对相同排班的待审批申请
        QueryWrapper<DoctorLeave> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", doctorId)
                .eq("status", "pending")
                .and(w -> {
                    for (int i = 0; i < param.getScheduleIds().size(); i++) {
                        Integer sid = param.getScheduleIds().get(i);
                        if (i == 0) {
                            w.apply("FIND_IN_SET({0}, schedule_ids)", sid);
                        } else {
                            w.or().apply("FIND_IN_SET({0}, schedule_ids)", sid);
                        }
                    }
                });

        if (doctorLeaveMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("所选排班中已有待审批的请假申请,请勿重复提交");
        }

        LocalDate minDate = targetSchedules.stream()
                .map(Schedule::getWorkDate)
                .min(LocalDate::compareTo)
                .orElse(today);
        LocalDate maxDate = targetSchedules.stream()
                .map(Schedule::getWorkDate)
                .max(LocalDate::compareTo)
                .orElse(today);

        DoctorLeave leave = new DoctorLeave();
        leave.setDoctorId(doctorId);
        leave.setFromDate(minDate);
        leave.setToDate(maxDate);
        leave.setReason(param.getReason());
        leave.setStatus("pending");
        leave.setAppliedBy(doctorId);
        leave.setAppliedAt(LocalDateTime.now());
        leave.setScheduleIds(param.getScheduleIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));

        doctorLeaveMapper.insert(leave);
    }

    @Override
    @Transactional
    public void applyScheduleAdjust(ScheduleAdjustParam param) {
        // 验证原排班是否存在
        Schedule originalSchedule = scheduleMapper.selectById(param.getScheduleId());
        if (originalSchedule == null) {
            throw new RuntimeException("原排班不存在");
        }

        if ("cancelled".equals(originalSchedule.getStatus())) {
            throw new RuntimeException("已取消的排班不能调整");
        }

        // 验证调整后的时间是否冲突
        if (scheduleService.checkDoctorScheduleConflict(
                originalSchedule.getDoctorId(),
                param.getAdjustedDate(),
                param.getAdjustedTimeSlot())) {
            throw new RuntimeException("调整后的时间段已有排班");
        }

        // 创建调班申请（记录到 schedule_exceptions）
        ScheduleException exception = new ScheduleException();
        exception.setScheduleId(param.getScheduleId());
        exception.setDoctorId(originalSchedule.getDoctorId());
        exception.setStartDate(originalSchedule.getWorkDate());
        exception.setEndDate(originalSchedule.getWorkDate());
        exception.setExceptionType("partial_adjust");
        exception.setAdjustedDate(param.getAdjustedDate());
        exception.setAdjustedTimeSlot(param.getAdjustedTimeSlot());
        exception.setAdjustedRoomId(param.getAdjustedRoomId());
        exception.setReason(param.getReason());
        exception.setCreatedBy(param.getAppliedBy());
        exception.setStatus("pending");
        exception.setCreatedAt(LocalDateTime.now());

        scheduleExceptionMapper.insert(exception);
    }

    @Override
    @Transactional
    public void reviewLeave(LeaveReviewParam param) {
        DoctorLeave leave = doctorLeaveMapper.selectById(param.getLeaveId());
        if (leave == null) {
            throw new RuntimeException("请假申请不存在");
        }

        if (!"pending".equals(leave.getStatus())) {
            throw new RuntimeException("该请假申请已审批");
        }

        // 更新请假状态
        if ("approve".equals(param.getAction())) {
            leave.setStatus("approved");

            List<Integer> leaveScheduleIds = leave.safeScheduleIds();
            List<Schedule> affectedSchedules;

            if (!leaveScheduleIds.isEmpty()) {
                QueryWrapper<Schedule> q = new QueryWrapper<>();
                q.in("schedule_id", leaveScheduleIds)
                        .ne("status", "cancelled");
                affectedSchedules = scheduleMapper.selectList(q);
            } else {
                // 兼容旧数据：按日期范围取消
                QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
                wrapper.eq("doctor_id", leave.getDoctorId())
                        .between("work_date", leave.getFromDate(), leave.getToDate())
                        .ne("status", "cancelled");
                affectedSchedules = scheduleMapper.selectList(wrapper);
            }

            // ✅ 冻结受影响预约人数（审批通过时只算一次）
            int affectedAppointmentCount;
            if (leave.getScheduleIds() != null && !leave.getScheduleIds().trim().isEmpty()) {
                affectedAppointmentCount = appointmentService.countAffectedAppointments(leave.getScheduleIds());
            } else {
                // 兼容旧数据：没有 scheduleIds 时，用 affectedSchedules 统计
                List<Integer> sids = affectedSchedules.stream()
                        .map(Schedule::getScheduleId)
                        .collect(Collectors.toList());

                if (sids.isEmpty()) {
                    affectedAppointmentCount = 0;
                } else {
                    QueryWrapper<Appointment> qw = new QueryWrapper<>();
                    qw.in("schedule_id", sids)
                            .in("appointment_status", "pending", "booked");
                    affectedAppointmentCount = Math.toIntExact(appointmentMapper.selectCount(qw));
                }
            }

            leave.setAffectedAppointmentCount(affectedAppointmentCount);
            leave.setStatus("approved");

            for (Schedule schedule : affectedSchedules) {
                schedule.setStatus("cancelled");
                schedule.setUpdatedAt(LocalDateTime.now());
                scheduleMapper.updateById(schedule);

                ScheduleException exception = new ScheduleException();
                exception.setScheduleId(schedule.getScheduleId());
                exception.setDoctorId(schedule.getDoctorId());
                exception.setStartDate(schedule.getWorkDate());
                exception.setEndDate(schedule.getWorkDate());
                exception.setExceptionType("leave");
                exception.setReason("医生请假：" + leave.getReason());
                exception.setCreatedBy(param.getReviewedBy());
                exception.setCreatedAt(LocalDateTime.now());
                scheduleExceptionMapper.insert(exception);

                reassignAppointments(schedule, param.getReviewedBy(), exception.getExceptionId());
            }

        } else if ("reject".equals(param.getAction())) {
            leave.setStatus("rejected");
        } else {
            throw new RuntimeException("无效的审批操作");
        }

        leave.setReviewedBy(param.getReviewedBy());
        leave.setReviewedAt(LocalDateTime.now());
        doctorLeaveMapper.updateById(leave);
    }

    @Override
    @Transactional
    public void reviewScheduleAdjust(Long exceptionId, String action, Long reviewedBy) {
        ScheduleException exception = scheduleExceptionMapper.selectById(exceptionId);
        if (exception == null) {
            throw new RuntimeException("调班申请不存在");
        }

        if (!"pending".equals(exception.getStatus())) {
            throw new RuntimeException("该调班申请已审批");
        }

        if ("approve".equals(action)) {
            exception.setStatus("approved");

            // 获取原排班
            Schedule originalSchedule = scheduleMapper.selectById(exception.getScheduleId());
            if (originalSchedule == null) {
                throw new RuntimeException("原排班不存在");
            }

            // 创建新排班
            Schedule newSchedule = new Schedule();
            newSchedule.setDoctorId(originalSchedule.getDoctorId());
            newSchedule.setDeptId(originalSchedule.getDeptId());
            newSchedule.setRoomId(exception.getAdjustedRoomId() != null ? exception.getAdjustedRoomId()
                    : originalSchedule.getRoomId());
            newSchedule.setWorkDate(exception.getAdjustedDate());
            newSchedule.setTimeSlot(exception.getAdjustedTimeSlot());
            newSchedule.setAppointmentTypeId(originalSchedule.getAppointmentTypeId());
            newSchedule.setMaxSlots(originalSchedule.getMaxSlots());
            newSchedule.setAvailableSlots(originalSchedule.getAvailableSlots());
            newSchedule.setStatus("open");
            newSchedule.setCreatedAt(LocalDateTime.now());
            newSchedule.setUpdatedAt(LocalDateTime.now());
            scheduleMapper.insert(newSchedule);

            // 取消原排班
            originalSchedule.setStatus("cancelled");
            originalSchedule.setUpdatedAt(LocalDateTime.now());
            scheduleMapper.updateById(originalSchedule);

            // 记录 exception（已经存在 exceptionId，但为安全，我们也更新）
            exception.setAdjustedDate(exception.getAdjustedDate());
            exception.setAdjustedRoomId(exception.getAdjustedRoomId());
            exception.setAdjustedTimeSlot(exception.getAdjustedTimeSlot());
            scheduleExceptionMapper.updateById(exception);

            // 迁移/通知已挂号患者：把原 schedule 中的 appointment 尝试系统分配到同科室同日期同时间段的其它医生
            reassignAppointments(originalSchedule, reviewedBy, exception.getExceptionId());

        } else if ("reject".equals(action)) {
            exception.setStatus("rejected");
        } else {
            throw new RuntimeException("无效的审批操作");
        }

        scheduleExceptionMapper.updateById(exception);
    }

    /**
     * 处理取消排班后的挂号迁移逻辑（保留旧预约 + 新建新预约）：
     * 1. 有替代排班：原预约标记为 system_cancel，新建预约 pending_patient_confirm
     * 2. 无替代排班：原预约 waiting_patient_action
     * 3. 全流程通知患者
     */
    @Transactional(rollbackFor = Exception.class)
    public void reassignAppointments(Schedule cancelledSchedule, Long operatorId, Long exceptionId) {

        List<Appointment> appointments = appointmentMapper.selectByScheduleId(cancelledSchedule.getScheduleId());
        if (appointments == null || appointments.isEmpty()) {
            log.info("【排班取消】排班 {} 无挂号，无需处理", cancelledSchedule.getScheduleId());
            return;
        }

        for (Appointment oldApp : appointments) {

            Schedule alternative = findAlternativeSchedule(
                    cancelledSchedule.getDeptId(),
                    cancelledSchedule.getWorkDate(),
                    cancelledSchedule.getTimeSlot()
            );

            // 系统自动取消并退款
            boolean cancelled = appointmentService.cancelAppointment(
                    oldApp.getAppointmentId(),
                    oldApp.getPatientId(),
                    2
            );

            if (!cancelled) {
                log.error("旧预约 {} 自动取消（含退款）失败", oldApp.getAppointmentId());
                continue;
            }

            if (alternative != null && alternative.getAvailableSlots() != null && alternative.getAvailableSlots() > 0) {

                alternative.setAvailableSlots(alternative.getAvailableSlots() - 1);
                scheduleMapper.updateById(alternative);

                Appointment newApp = new Appointment();
                newApp.setPatientId(oldApp.getPatientId());
                newApp.setDeptId(oldApp.getDeptId());
                newApp.setScheduleId(alternative.getScheduleId());
                newApp.setRoomId(alternative.getRoomId());
                newApp.setAppointmentTypeId(alternative.getAppointmentTypeId());

                Integer maxQueue = appointmentMapper.getMaxQueueNumberByScheduleId(Long.valueOf(alternative.getScheduleId()));
                newApp.setQueueNumber((maxQueue == null ? 0 : maxQueue) + 1);

                newApp.setPaymentStatus("unpaid");
                newApp.setAppointmentStatus("pending_patient_confirm");
                newApp.setFeeOriginal(oldApp.getFeeOriginal());
                newApp.setFeeFinal(oldApp.getFeeFinal());
                newApp.setCreatedAt(LocalDateTime.now());

                appointmentMapper.insert(newApp);

                // 关联记录
                AppointmentRelations rel = new AppointmentRelations();
                rel.setSourceAppointmentId(oldApp.getAppointmentId());
                rel.setTargetAppointmentId(newApp.getAppointmentId());
                rel.setRelationType("AUTO_REASSIGN_REFUND");
                rel.setRemark("排班取消，原预约已自动退款，新建待支付预约");
                rel.setCreatedBy(operatorId);
                rel.setCreatedAt(LocalDateTime.now());
                appointmentRelationsMapper.insert(rel);

                // 通知
                notificationEmailService.sendAppointmentReassignNotification(
                        oldApp.getPatientId(),
                        cancelledSchedule.getScheduleId(),
                        alternative.getScheduleId(),
                        "因医生排班调整，原预约已自动取消并退款；系统已生成新的预约，请在 30 分钟内完成支付。"
                );

                log.info("旧预约 {} 自动退款，新建预约 {}，替代排班 {}。",
                        oldApp.getAppointmentId(),
                        newApp.getAppointmentId(),
                        alternative.getScheduleId());
                continue;
            }

            // 无替代排班，仅退款
            oldApp.setAppointmentStatus("waiting_patient_action");
            oldApp.setUpdatedAt(LocalDateTime.now());
            appointmentMapper.updateStatusOnly(oldApp);

            notificationEmailService.sendScheduleCancelledNotification(
                    oldApp.getPatientId(),
                    cancelledSchedule.getScheduleId(),
                    "因医生排班取消，您的预约已自动取消并退款，当前无可替代排班，请重新选择其他排班挂号。"
            );

            log.info("旧预约 {} 自动退款（无替代排班）。", oldApp.getAppointmentId());
        }
    }


    /**
     * 查找替代排班（查询顺序、策略可按需求微调）
     */
    private Schedule findAlternativeSchedule(Integer deptId, java.time.LocalDate workDate, Integer timeSlot) {
        QueryWrapper<Schedule> q = new QueryWrapper<>();
        q.eq("dept_id", deptId)
                .eq("work_date", workDate)
                .eq("time_slot", timeSlot)
                .eq("status", "open")
                .gt("available_slots", 0)
                .orderByDesc("available_slots")
                .last("LIMIT 1");

        return scheduleMapper.selectOne(q);
    }

    @Override
    public List<DoctorLeaveVO> getLeaveApplications(String status, Long doctorId) {
        QueryWrapper<DoctorLeave> wrapper = new QueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        if (doctorId != null) {
            wrapper.eq("doctor_id", doctorId);
        }
        wrapper.orderByDesc("applied_at");

        List<DoctorLeave> leaves = doctorLeaveMapper.selectList(wrapper);
        return leaves.stream()
                .map(this::convertToLeaveVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleException> getAdjustApplications(String status, Long doctorId) {
        QueryWrapper<ScheduleException> wrapper = new QueryWrapper<>();
        wrapper.eq("exception_type", "partial_adjust");
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        if (doctorId != null) {
            wrapper.eq("doctor_id", doctorId);
        }
        wrapper.orderByDesc("created_at");

        return scheduleExceptionMapper.selectList(wrapper);
    }

    @Override
    public List<DoctorLeave> getDoctorLeaveHistory(Long userId) {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, userId));
        Long doctorId = doctor.getDoctorId();
        QueryWrapper<DoctorLeave> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", doctorId)
                .orderByDesc("applied_at");

        return doctorLeaveMapper.selectList(wrapper);
    }

    // 辅助方法：转换为LeaveVO
    private DoctorLeaveVO convertToLeaveVO(DoctorLeave leave) {
        DoctorLeaveVO vo = new DoctorLeaveVO();
        vo.setLeaveId(leave.getLeaveId());
        vo.setDoctorId(leave.getDoctorId());
        vo.setReason(leave.getReason());
        vo.setStatus(leave.getStatus());
        vo.setAppliedBy(leave.getAppliedBy());
        vo.setAppliedAt(leave.getAppliedAt());
        vo.setReviewedBy(leave.getReviewedBy());
        vo.setReviewedAt(leave.getReviewedAt());
        vo.setScheduleIds(leave.getScheduleIds());

        // 关联查询医生信息
        Doctor doctor = doctorMapper.selectById(leave.getDoctorId());
        if (doctor != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user != null) {
                vo.setDoctorName(user.getUsername());
            }

            Department dept = departmentMapper.selectById(doctor.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        // ============================
        // 新增：根据 schedule_ids 查询排班
        // ============================
        if (leave.getScheduleIds() != null && !leave.getScheduleIds().trim().isEmpty()) {
            // 1. 字符串转 Long 列表
            List<Long> ids = Arrays.stream(leave.getScheduleIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (!ids.isEmpty()) {
                // 2. 批量查询 schedule 表
                QueryWrapper<Schedule> scheduleQuery = new QueryWrapper<>();
                scheduleQuery.in("schedule_id", ids);
                List<Schedule> scheduleList = scheduleMapper.selectList(scheduleQuery);

                // 3. 为 VO 组装前端用的数据结构
                List<ScheduleInfoVO> infoList = scheduleList.stream().map(s -> {
                    ScheduleInfoVO si = new ScheduleInfoVO();
                    si.setScheduleId(s.getScheduleId());
                    si.setDate(s.getWorkDate()); // 按你的表字段名
                    si.setTimeSlot(s.getTimeSlot()); // 按你的表字段名
                    return si;
                }).collect(Collectors.toList());

                vo.setScheduleInfos(infoList);

                // 4. 顺便统计受影响排班数量
                vo.setAffectedScheduleCount(infoList.size());

                //这段逻辑不对。审批通过后就没有患者的状态还是"pending", "booked"了。应当冻结到审批之前的人数，只看审批前有多少人被影响
//                // 5. 统计受影响的挂号
//                int appointmentCount = 0;
//                for (Schedule s : scheduleList) {
//                    QueryWrapper<Appointment> appointmentWrapper = new QueryWrapper<>();
//                    appointmentWrapper.eq("schedule_id", s.getScheduleId())
//                            .in("appointment_status", "pending", "booked");
//                    appointmentCount += appointmentMapper.selectCount(appointmentWrapper);
//                }
//                vo.setAffectedAppointmentCount(appointmentCount);
                vo.setAffectedAppointmentCount(
                        leave.getAffectedAppointmentCount()
                );
            }
        } else {
            vo.setAffectedScheduleCount(0);
            vo.setAffectedAppointmentCount(0);
        }

        return vo;
    }

}
