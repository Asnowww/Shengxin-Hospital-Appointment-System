package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.dto.*;
import org.example.backend.mapper.*;
import org.example.backend.pojo.*;
import org.example.backend.service.DoctorLeaveService;
import org.example.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScheduleService scheduleService;

    @Override
    @Transactional
    public void applyLeave(LeaveApplyParam param) {
        // 验证医生是否存在
        Doctor doctor = doctorMapper.selectById(param.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        // 验证日期
        if (param.getFromDate().isAfter(param.getToDate())) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }

        // 检查是否有重叠的请假申请
        QueryWrapper<DoctorLeave> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", param.getDoctorId())
                .eq("status", "pending")
                .and(w -> w.between("from_date", param.getFromDate(), param.getToDate())
                        .or()
                        .between("to_date", param.getFromDate(), param.getToDate()));

        if (doctorLeaveMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该时间段已有待审核的请假申请");
        }

        // 创建请假申请
        DoctorLeave leave = new DoctorLeave();
        leave.setDoctorId(param.getDoctorId());
        leave.setFromDate(param.getFromDate());
        leave.setToDate(param.getToDate());
        leave.setReason(param.getReason());
        leave.setStatus("pending");
        leave.setAppliedBy(param.getDoctorId());
        leave.setAppliedAt(LocalDateTime.now());

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

            // 查询该时间段内该医生的所有排班
            QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
            wrapper.eq("doctor_id", leave.getDoctorId())
                    .between("work_date", leave.getFromDate(), leave.getToDate())
                    .ne("status", "cancelled");

            List<Schedule> affectedSchedules = scheduleMapper.selectList(wrapper);

            // 取消所有排班
            for (Schedule schedule : affectedSchedules) {
                schedule.setStatus("cancelled");
                schedule.setUpdatedAt(LocalDateTime.now());
                scheduleMapper.updateById(schedule);

                // 记录到 schedule_exceptions
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

                // TODO: 处理受影响的患者挂号（退款+通知）
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
            newSchedule.setRoomId(exception.getAdjustedRoomId() != null ?
                    exception.getAdjustedRoomId() : originalSchedule.getRoomId());
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

            // TODO: 处理已挂号患者（转移到新排班或退款）

        } else if ("reject".equals(action)) {
            exception.setStatus("rejected");
        } else {
            throw new RuntimeException("无效的审批操作");
        }

        scheduleExceptionMapper.updateById(exception);
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
    public List<DoctorLeave> getDoctorLeaveHistory(Long doctorId) {
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
        vo.setFromDate(leave.getFromDate());
        vo.setToDate(leave.getToDate());
        vo.setReason(leave.getReason());
        vo.setStatus(leave.getStatus());
        vo.setAppliedBy(leave.getAppliedBy());
        vo.setAppliedAt(leave.getAppliedAt());
        vo.setReviewedBy(leave.getReviewedBy());
        vo.setReviewedAt(leave.getReviewedAt());

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

        // 统计受影响的排班和挂号
        QueryWrapper<Schedule> scheduleWrapper = new QueryWrapper<>();
        scheduleWrapper.eq("doctor_id", leave.getDoctorId())
                .between("work_date", leave.getFromDate(), leave.getToDate());
        vo.setAffectedScheduleCount(Math.toIntExact(scheduleMapper.selectCount(scheduleWrapper)));

        // 统计受影响的挂号数量
        List<Schedule> affectedSchedules = scheduleMapper.selectList(scheduleWrapper);
        int affectedAppointmentCount = 0;
        for (Schedule schedule : affectedSchedules) {
            QueryWrapper<Appointment> appointmentWrapper = new QueryWrapper<>();
            appointmentWrapper.eq("schedule_id", schedule.getScheduleId())
                    .in("appointment_status", "pending", "booked");
            affectedAppointmentCount += Math.toIntExact(appointmentMapper.selectCount(appointmentWrapper));
        }
        vo.setAffectedAppointmentCount(affectedAppointmentCount);

        return vo;
    }
}
