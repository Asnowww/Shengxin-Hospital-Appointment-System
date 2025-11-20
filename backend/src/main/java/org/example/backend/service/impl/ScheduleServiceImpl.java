package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.dto.*;
import org.example.backend.mapper.*;
import org.example.backend.pojo.*;
import org.example.backend.service.ScheduleService;
import org.example.backend.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ConsultationRoomMapper consultationRoomMapper;

    @Autowired
    private AppointmentTypeMapper appointmentTypeMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScheduleExceptionMapper scheduleExceptionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WaitlistService waitlistService;


    @Override
    @Transactional
    public void batchCreateSchedules(ScheduleCreateParam param) {
        // 验证医生、科室、诊室是否存在
        Doctor doctor = doctorMapper.selectById(param.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        Department department = departmentMapper.selectById(param.getDeptId());
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        ConsultationRoom room = consultationRoomMapper.selectById(param.getRoomId());
        if (room == null) {
            throw new RuntimeException("诊室不存在");
        }

        List<Schedule> schedulesToInsert = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        LocalDate currentDate = param.getStartDate();
        while (!currentDate.isAfter(param.getEndDate())) {
            // 如果设置了 weekDays，只在这些天创建
            if (param.getWeekdays() != null && !param.getWeekdays().isEmpty()) {
                int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=周一, 7=周日
                if (!param.getWeekdays().contains(dayOfWeek)) {
                    currentDate = currentDate.plusDays(1);
                    continue;
                }
            }

            // 遍历时间段
            for (Integer timeSlot : param.getTimeSlots()) {
                // 检查是否已有排班（防止重复）
                if (checkDoctorScheduleConflict(param.getDoctorId(), currentDate, timeSlot)) {
                    errors.add(String.format("医生在 %s 的 %s 时段已有排班", currentDate, getTimeSlotName(timeSlot)));
                    continue; // 不再创建
                }

                Schedule schedule = new Schedule();
                schedule.setDoctorId(param.getDoctorId());
                schedule.setDeptId(param.getDeptId());
                schedule.setRoomId(param.getRoomId());
                schedule.setWorkDate(currentDate);
                schedule.setTimeSlot(timeSlot);
                schedule.setAppointmentTypeId(param.getAppointmentTypeId());
                schedule.setMaxSlots(param.getMaxSlots());
                schedule.setAvailableSlots(param.getMaxSlots());
                schedule.setStatus("open");
                schedule.setCreatedAt(LocalDateTime.now());
                schedule.setUpdatedAt(LocalDateTime.now());

                schedulesToInsert.add(schedule);
            }

            currentDate = currentDate.plusDays(1);
        }

        // 如果有冲突错误，直接报错，不插入
        if (!errors.isEmpty()) {
            throw new RuntimeException("批量创建排班失败:\n" + String.join("\n", errors));
        }

        // 如果没有创建任何排班（即所有日期都不匹配 weekDays），则直接报错
        if (schedulesToInsert.isEmpty()) {
            throw new RuntimeException("所选日期范围内没有匹配的星期几，未创建任何排班");
        }


        // 插入
        for (Schedule schedule : schedulesToInsert) {
            scheduleMapper.insert(schedule);
        }
    }


    @Override
    @Transactional
    public void createSchedules(ScheduleCreateParam param) {
        // 1️⃣ 验证医生、科室、诊室是否存在
        Doctor doctor = doctorMapper.selectById(param.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        Department department = departmentMapper.selectById(param.getDeptId());
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        ConsultationRoom room = consultationRoomMapper.selectById(param.getRoomId());
        if (room == null) {
            throw new RuntimeException("诊室不存在");
        }

        // 2️⃣ 验证日期与时间段
        LocalDate workDate = param.getStartDate(); // 原来的
        if (workDate == null && param.getDate() != null) {
            workDate = param.getDate();
        }
        if (workDate == null) {
            throw new RuntimeException("排班日期不能为空");
        }

        if (param.getTimeSlots() == null || param.getTimeSlots().isEmpty()) {
            throw new RuntimeException("必须选择至少一个时间段");
        }

//        LocalDate workDate = param.getStartDate();
        List<Schedule> schedulesToInsert = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 3️⃣ 遍历选择的时间段
        for (Integer timeSlot : param.getTimeSlots()) {
            // 检查是否已有排班
            if (checkDoctorScheduleConflict(param.getDoctorId(), workDate, timeSlot)) {
                errors.add(String.format("医生在 %s 的 %s 时段已有排班", workDate, getTimeSlotName(timeSlot)));
                continue;
            }

            // 构造排班对象
            Schedule schedule = new Schedule();
            schedule.setDoctorId(param.getDoctorId());
            schedule.setDeptId(param.getDeptId());
            schedule.setRoomId(param.getRoomId());
            schedule.setWorkDate(workDate);
            schedule.setTimeSlot(timeSlot);
            schedule.setAppointmentTypeId(param.getAppointmentTypeId());
            schedule.setMaxSlots(param.getMaxSlots());
            schedule.setAvailableSlots(param.getMaxSlots());
            schedule.setStatus("open");
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());

            schedulesToInsert.add(schedule);
        }

        // 4️⃣ 如果所有时间段都冲突，直接报错
        if (schedulesToInsert.isEmpty()) {
            throw new RuntimeException("所选时间段都已被占用，未创建任何排班:\n" + String.join("\n", errors));
        }

        // 5️⃣ 插入数据库
        for (Schedule schedule : schedulesToInsert) {
            scheduleMapper.insert(schedule);
        }

        // 6️⃣ 如果部分时间段冲突，提示警告
        if (!errors.isEmpty()) {
            throw new RuntimeException("部分时间段未创建:\n" + String.join("\n", errors));
        }
    }


    @Override
    @Transactional
    public void updateSchedule(ScheduleUpdateParam param) {
        Schedule schedule = scheduleMapper.selectById(param.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 如果修改为cancelled，需要处理已挂号患者
        if ("cancelled".equals(param.getStatus()) && !"cancelled".equals(schedule.getStatus())) {
            if (param.getCancelReason() == null || param.getCancelReason().trim().isEmpty()) {
                throw new RuntimeException("取消排班必须提供原因");
            }
            // 这里会在后续处理受影响的患者
        }

        // 更新排班信息
        if (param.getRoomId() != null) {
            ConsultationRoom room = consultationRoomMapper.selectById(param.getRoomId());
            if (room == null) {
                throw new RuntimeException("诊室不存在");
            }
            if (!room.getDeptId().equals(schedule.getDeptId())) {
                throw new RuntimeException("诊室不属于该科室");
            }
            schedule.setRoomId(param.getRoomId());
        }

        if (param.getTimeSlot() != null) {
            // 检查新时间段是否冲突
            if (checkDoctorScheduleConflict(schedule.getDoctorId(), schedule.getWorkDate(), param.getTimeSlot())) {
                throw new RuntimeException("新时间段已有排班");
            }
            schedule.setTimeSlot(param.getTimeSlot());
        }

        if (param.getMaxSlots() != null) {
            int bookedSlots = schedule.getMaxSlots() - schedule.getAvailableSlots();
            if (param.getMaxSlots() < bookedSlots) {
                throw new RuntimeException(String.format("已预约 %d 个号，不能设置最大号源数小于已预约数", bookedSlots));
            }
            schedule.setAvailableSlots(param.getMaxSlots() - bookedSlots);
            schedule.setMaxSlots(param.getMaxSlots());
        }

        if (param.getStatus() != null) {
            schedule.setStatus(param.getStatus());
        }

        schedule.setUpdatedAt(LocalDateTime.now());
        scheduleMapper.updateById(schedule);

        // 如果是取消，记录到 schedule_exceptions
        if ("cancelled".equals(param.getStatus())) {
            ScheduleException exception = new ScheduleException();
            exception.setScheduleId(schedule.getScheduleId());
            exception.setDoctorId(schedule.getDoctorId());
            exception.setStartDate(schedule.getWorkDate());
            exception.setEndDate(schedule.getWorkDate());
            exception.setExceptionType("cancel_all");
            exception.setReason(param.getCancelReason());
            exception.setCreatedAt(LocalDateTime.now());
            scheduleExceptionMapper.insert(exception);
        }
    }

    @Override
    @Transactional
    public void cancelSchedule(Integer scheduleId, String reason, Long operatorId) {
        ScheduleUpdateParam param = new ScheduleUpdateParam();
        param.setScheduleId(scheduleId);
        param.setStatus("cancelled");
        param.setCancelReason(reason);
        updateSchedule(param);
    }

    @Override
    public ScheduleDetailVO getScheduleDetail(Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return null;
        }

        return convertToDetailVO(schedule);
    }

    @Override
    public List<ScheduleDetailVO> getDoctorSchedules(Long userId, LocalDate startDate, LocalDate endDate) {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, userId)
        );
        Long doctorId = doctor.getDoctorId();

        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", doctorId);
        if (startDate != null) {
            wrapper.ge("work_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("work_date", endDate);
        }
        wrapper.orderByAsc("work_date", "time_slot");

        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        return schedules.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDetailVO> getDepartmentSchedules(Integer deptId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.eq("dept_id", deptId);
        if (startDate != null) {
            wrapper.ge("work_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("work_date", endDate);
        }
        wrapper.orderByAsc("work_date", "time_slot");

        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        return schedules.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDetailVO> getAllSchedules(Integer deptId, Long doctorId, LocalDate startDate, LocalDate endDate, String status) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        if (deptId != null) {
            wrapper.eq("dept_id", deptId);
        }
        if (doctorId != null) {
            wrapper.eq("doctor_id", doctorId);
        }
        if (startDate != null) {
            wrapper.ge("work_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("work_date", endDate);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("work_date", "time_slot");

        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        return schedules.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addExtraSlots(AddExtraSlotsParam param) {
        Schedule schedule = scheduleMapper.selectById(param.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }
        if ("cancelled".equals(schedule.getStatus())) {
            throw new RuntimeException("已停诊的排班不能加号");
        }

        schedule.setMaxSlots(schedule.getMaxSlots() + param.getExtraSlots());
        schedule.setAvailableSlots(schedule.getAvailableSlots() + param.getExtraSlots());
        schedule.setUpdatedAt(LocalDateTime.now());
        scheduleMapper.updateById(schedule);

        ScheduleException exception = new ScheduleException();
        exception.setScheduleId(schedule.getScheduleId());
        exception.setDoctorId(schedule.getDoctorId());
        exception.setStartDate(schedule.getWorkDate());
        exception.setEndDate(schedule.getWorkDate());
        exception.setExceptionType("special_add");
        exception.setExtraSlots(param.getExtraSlots());
        exception.setReason(param.getReason());
        exception.setCreatedBy(param.getCreatedBy());
        exception.setCreatedAt(LocalDateTime.now());
        scheduleExceptionMapper.insert(exception);

        // ===== 新增：处理候补队列 =====
        try {
            waitlistService.processWaitlistConversion(param.getScheduleId());
        } catch (Exception e) {
            System.err.println("处理候补队列失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkDoctorScheduleConflict(Long doctorId, LocalDate workDate, Integer timeSlot) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", doctorId)
                .eq("work_date", workDate)
                .eq("time_slot", timeSlot)
                .ne("status", "cancelled"); // 不考虑已取消的排班

        return scheduleMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Long> getAffectedAppointments(Integer scheduleId) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("schedule_id", scheduleId)
                .in("appointment_status", "pending", "booked"); // 只统计待支付和已预约的

        List<Appointment> appointments = appointmentMapper.selectList(wrapper);
        return appointments.stream()
                .map(Appointment::getAppointmentId)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlternativeScheduleVO> recommendAlternativeSchedules(Integer scheduleId, int limit) {
        Schedule originalSchedule = scheduleMapper.selectById(scheduleId);
        if (originalSchedule == null) {
            return new ArrayList<>();
        }

        // 查询同科室、同时间段、状态为open的排班
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.eq("dept_id", originalSchedule.getDeptId())
                .eq("work_date", originalSchedule.getWorkDate())
                .eq("time_slot", originalSchedule.getTimeSlot())
                .eq("status", "open")
                .ne("schedule_id", scheduleId) // 排除原排班
                .gt("available_slots", 0); // 有剩余号源

        List<Schedule> alternatives = scheduleMapper.selectList(wrapper);

        return alternatives.stream()
                .limit(limit)
                .map(this::convertToAlternativeVO)
                .collect(Collectors.toList());
    }

    // 辅助方法：转换为详情VO
    private ScheduleDetailVO convertToDetailVO(Schedule schedule) {
        ScheduleDetailVO vo = new ScheduleDetailVO();
        vo.setScheduleId(schedule.getScheduleId());
        vo.setDoctorId(schedule.getDoctorId());
        vo.setDeptId(schedule.getDeptId());
        vo.setRoomId(schedule.getRoomId());
        vo.setWorkDate(schedule.getWorkDate());
        vo.setTimeSlot(schedule.getTimeSlot());
        vo.setTimeSlotName(getTimeSlotName(schedule.getTimeSlot()));
        vo.setAppointmentTypeId(schedule.getAppointmentTypeId());
        vo.setMaxSlots(schedule.getMaxSlots());
        vo.setAvailableSlots(schedule.getAvailableSlots());
        vo.setBookedSlots(schedule.getMaxSlots() - schedule.getAvailableSlots());
        vo.setStatus(schedule.getStatus());
        vo.setCreatedAt(schedule.getCreatedAt());
        vo.setUpdatedAt(schedule.getUpdatedAt());

        // 关联查询医生信息
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        if (doctor != null) {
            // 查询医生对应的用户信息获取姓名
            User user = userMapper.selectById(doctor.getUserId());
            if (user != null) {
                vo.setDoctorName(user.getName());
            }
            vo.setDoctorTitle(doctor.getTitle());

        }

        // 关联查询科室信息
        Department dept = departmentMapper.selectById(schedule.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getDeptName());
        }

        // 关联查询诊室信息
        ConsultationRoom room = consultationRoomMapper.selectById(schedule.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getRoomName());
        }

        // 关联查询号别信息
        AppointmentType appointmentType = appointmentTypeMapper.selectById(schedule.getAppointmentTypeId());
        if (appointmentType != null) {
            vo.setAppointmentTypeName(appointmentType.getTypeName());
        }

        return vo;
    }

    // 辅助方法：转换为替代排班VO
    private AlternativeScheduleVO convertToAlternativeVO(Schedule schedule) {
        AlternativeScheduleVO vo = new AlternativeScheduleVO();
        vo.setScheduleId(schedule.getScheduleId());
        vo.setDoctorId(schedule.getDoctorId());
        vo.setWorkDate(schedule.getWorkDate());
        vo.setTimeSlot(schedule.getTimeSlot());
        vo.setTimeSlotName(getTimeSlotName(schedule.getTimeSlot()));
        vo.setAvailableSlots(schedule.getAvailableSlots());

        // 关联查询
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        if (doctor != null) {
            vo.setDoctorTitle(doctor.getTitle());
            // 查询医生对应的用户信息获取姓名
            User user = userMapper.selectById(doctor.getUserId());
            if (user != null) {
                vo.setDoctorName(user.getName());
            }
        }

        Department dept = departmentMapper.selectById(schedule.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getDeptName());
        }

        ConsultationRoom room = consultationRoomMapper.selectById(schedule.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getRoomName());
        }

        AppointmentType appointmentType = appointmentTypeMapper.selectById(schedule.getAppointmentTypeId());
        if (appointmentType != null) {
            vo.setAppointmentTypeName(appointmentType.getTypeName());
        }

        return vo;
    }

    // 辅助方法：时间段转名称
    private String getTimeSlotName(Integer timeSlot) {
        switch (timeSlot) {
            case 0:
                return "上午";
            case 1:
                return "下午";
            case 2:
                return "晚上";
            default:
                return "未知";
        }
    }

    /**
     * 统计排班利用率（已预约/总号源）
     */
    @Override
    public List<ScheduleUtilizationStats> getScheduleUtilizationStats(LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectScheduleUtilizationStats(startDate, endDate);
    }

    /**
     * 统计排班覆盖率（有排班的天数/总天数）
     */
    @Override
    public List<ScheduleCoverageStats> getDepartmentScheduleCoverageStats(LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectScheduleCoverageStats(startDate, endDate);
    }

}
