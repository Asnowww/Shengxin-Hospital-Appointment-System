package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.AppointmentCreateParam;
import org.example.backend.dto.AppointmentUpdateParam;
import org.example.backend.mapper.AppointmentMapper;
import org.example.backend.mapper.AppointmentTypeMapper;
import org.example.backend.mapper.ScheduleMapper;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.AppointmentType;
import org.example.backend.pojo.Schedule;
import org.example.backend.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private AppointmentTypeMapper appointmentTypeMapper;

    // === 病人端 ===

    @Override
    @Transactional
    public Appointment createAppointmentByPatient(AppointmentCreateParam param) {
        // 1. 验证排班是否存在
        Schedule schedule = scheduleMapper.selectById(param.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 2. 验证排班状态
        if (!"open".equals(schedule.getStatus())) {
            throw new RuntimeException("该排班已停诊，无法预约");
        }

        // 3. 验证是否有剩余号源
        if (schedule.getAvailableSlots() <= 0) {
            throw new RuntimeException("号源已满，无法预约");
        }

        // 4. 验证患者当天是否已有同科室预约（防止重复挂号）
        QueryWrapper<Appointment> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("patient_id", param.getPatientId())
                .eq("schedule_id", param.getScheduleId())
                .in("appointment_status", "pending", "booked");
        Long existCount = appointmentMapper.selectCount(checkWrapper);
        if (existCount > 0) {
            throw new RuntimeException("您已预约该排班，请勿重复挂号");
        }

        // 5. 查询号别费用
        AppointmentType appointmentType = appointmentTypeMapper.selectById(schedule.getAppointmentTypeId());
        if (appointmentType == null) {
            throw new RuntimeException("号别类型不存在");
        }

        // 6. 生成排队号（当前已预约数 + 1）
        int queueNumber = schedule.getMaxSlots() - schedule.getAvailableSlots() + 1;

        // 7. 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(param.getPatientId());
        appointment.setScheduleId(param.getScheduleId());
        appointment.setDeptId(schedule.getDeptId());
        appointment.setRoomId(schedule.getRoomId());
        appointment.setAppointmentTypeId(schedule.getAppointmentTypeId());
        appointment.setQueueNumber(queueNumber);

        // 设置费用
        appointment.setFeeOriginal(appointmentType.getFee());
        appointment.setFeeFinal(appointmentType.getFee()); // 可以在这里应用折扣逻辑

        // 设置状态
        appointment.setPaymentStatus("unpaid");  // 待支付
        appointment.setAppointmentStatus("pending");  // 待支付状态

        // 设置时间
        appointment.setBookingTime(LocalDateTime.now());
        // 设置支付过期时间（30分钟后）
        appointment.setExpireTime(LocalDateTime.now().plusMinutes(30));

        // 设置备注
        if (param.getNotes() != null && !param.getNotes().trim().isEmpty()) {
            appointment.setNotes(param.getNotes());
        }

        appointment.setCreatedBy(param.getPatientId());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        // 8. 保存预约
        int result = appointmentMapper.insert(appointment);
        if (result <= 0) {
            throw new RuntimeException("预约创建失败");
        }

        // 9. 减少排班可用号源
        schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
        schedule.setUpdatedAt(LocalDateTime.now());
        scheduleMapper.updateById(schedule);

        return appointment;
    }


    @Override
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentMapper.selectList(
                new QueryWrapper<Appointment>().eq("patient_id", patientId)
        );
    }

    @Override
    public List<Appointment> getAppointmentsByPatientIdAndDate(Long patientId, LocalDate date) {
        return appointmentMapper.selectList(
                new QueryWrapper<Appointment>()
                        .eq("patient_id", patientId)
                        .apply("DATE(appointment_date) = {0}", date)
        );
    }

    @Override
    public Appointment getById(Long appointmentId) {
        return appointmentMapper.selectById(appointmentId);
    }

    @Override
    public boolean cancelAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getPatientId().equals(patientId)) {
            return false; // 非本人或不存在
        }
        // 如果已支付，顺便改 payment_status 为 refunded
        if ("paid".equals(appointment.getPaymentStatus())) {
            appointment.setPaymentStatus("refunded");
        }

        appointment.setAppointmentStatus("cancelled");
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Transactional
    @Override
    public boolean updateAppointment(AppointmentUpdateParam param) {
        // 1. 验证预约是否存在且属于该患者
        Appointment appointment = appointmentMapper.selectById(param.getAppointmentId());
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        if (!appointment.getPatientId().equals(param.getPatientId())) {
            throw new RuntimeException("无权修改该预约");
        }

        // 2. 验证预约状态是否允许修改
        if (!"booked".equals(appointment.getAppointmentStatus())
                && !"pending".equals(appointment.getAppointmentStatus())) {
            throw new RuntimeException("当前预约状态不允许修改");
        }

        // 3. 如果修改排班（改期改时间）
        if (param.getNewScheduleId() != null &&
                !param.getNewScheduleId().equals(appointment.getScheduleId())) {

            // 查询新排班
            Schedule newSchedule = scheduleMapper.selectById(param.getNewScheduleId());
            if (newSchedule == null) {
                throw new RuntimeException("新排班不存在");
            }

            // 验证新排班状态
            if (!"open".equals(newSchedule.getStatus())) {
                throw new RuntimeException("新排班已停诊");
            }

            if (newSchedule.getAvailableSlots() <= 0) {
                throw new RuntimeException("新排班号源已满");
            }

            // 恢复原排班号源
            Schedule oldSchedule = scheduleMapper.selectById(appointment.getScheduleId());
            if (oldSchedule != null) {
                oldSchedule.setAvailableSlots(oldSchedule.getAvailableSlots() + 1);
                oldSchedule.setUpdatedAt(LocalDateTime.now());
                scheduleMapper.updateById(oldSchedule);
            }

            // 减少新排班号源
            newSchedule.setAvailableSlots(newSchedule.getAvailableSlots() - 1);
            newSchedule.setUpdatedAt(LocalDateTime.now());
            scheduleMapper.updateById(newSchedule);

            // 更新预约信息
            appointment.setScheduleId(param.getNewScheduleId());
            appointment.setDeptId(newSchedule.getDeptId());
            appointment.setRoomId(newSchedule.getRoomId());

            // 重新生成排队号（需要根据新排班的已预约数量）
            int newQueueNumber = newSchedule.getMaxSlots() - newSchedule.getAvailableSlots();
            appointment.setQueueNumber(newQueueNumber);
        }

        // 4. 更新备注
        if (param.getNotes() != null) {
            appointment.setNotes(param.getNotes());
        }

        // 5. 更新支付状态和预约状态
        if (param.getPaymentStatus() != null) {
            appointment.setPaymentStatus(param.getPaymentStatus());
        }
        if (param.getAppointmentStatus() != null) {
            appointment.setAppointmentStatus(param.getAppointmentStatus());
        }

        // 6. 更新时间戳
        appointment.setUpdatedAt(LocalDateTime.now());



        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public boolean canUpdateAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return false;
        }

        // 验证患者身份
        if (!appointment.getPatientId().equals(patientId)) {
            return false;
        }

        // 只有待支付和已预约状态可以修改
        String status = appointment.getAppointmentStatus();
        if (!"booked".equals(status) && !"pending".equals(status)) {
            return false;
        }

        // 验证预约时间未过期
        if (appointment.getExpireTime() != null &&
                LocalDateTime.now().isAfter(appointment.getExpireTime())) {
            return false;
        }

        return true;
    }


    // === 医生端 ===

    @Override
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentMapper.selectAppointmentsByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> getTodayAppointmentsByDoctorId(Long doctorId) {
        return appointmentMapper.selectAppointmentsByDoctorIdAndDate(doctorId, LocalDate.now());
    }

    // === 管理员端 ===

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentMapper.selectList(null);
    }

    @Override
    public boolean updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return false;
        }
        appointment.setAppointmentStatus(status);
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public boolean updateAppointmentFee(Integer id, Double fee){
        AppointmentType type = appointmentTypeMapper.selectById(id);
        if (type == null) return false;
        type.setFeeAmount(java.math.BigDecimal.valueOf(fee));
        return appointmentTypeMapper.updateById(type) > 0;
    }
}