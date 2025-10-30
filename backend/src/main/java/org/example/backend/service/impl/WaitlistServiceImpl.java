package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.AppointmentCreateParam;
import org.example.backend.dto.WaitlistCreateParam;
import org.example.backend.mapper.AppointmentMapper;
import org.example.backend.mapper.ScheduleMapper;
import org.example.backend.mapper.WaitlistMapper;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.Schedule;
import org.example.backend.pojo.Waitlist;
import org.example.backend.service.AppointmentService;
import org.example.backend.service.NotificationEmailService;
import org.example.backend.service.WaitlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaitlistServiceImpl extends ServiceImpl<WaitlistMapper, Waitlist> implements WaitlistService {

    @Resource
    private WaitlistMapper waitlistMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private NotificationEmailService notificationEmailService;

    /**
     * 创建候补预约
     */
    @Override
    @Transactional
    public Waitlist createWaitlist(WaitlistCreateParam param) {
        // 1. 验证排班是否存在
        Schedule schedule = scheduleMapper.selectById(param.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 2. 验证排班状态
        if (!"open".equals(schedule.getStatus())) {
            throw new RuntimeException("该排班已停诊，无法候补");
        }

        // 3. 验证是否还有号源（有号源就不需要候补）
        if (schedule.getAvailableSlots() > 0) {
            throw new RuntimeException("该排班还有号源，请直接预约");
        }

        // 4. 验证患者是否已有该排班的候补
        QueryWrapper<Waitlist> waitlistCheck = new QueryWrapper<>();
        waitlistCheck.eq("patient_id", param.getPatientId())
                .eq("schedule_id", param.getScheduleId())
                .eq("status", "waiting");
        Long waitlistCount = waitlistMapper.selectCount(waitlistCheck);
        if (waitlistCount > 0) {
            throw new RuntimeException("您已在该排班的候补队列中");
        }

        // 5. 验证患者是否已有该排班的预约
        QueryWrapper<Appointment> appointmentCheck = new QueryWrapper<>();
        appointmentCheck.eq("patient_id", param.getPatientId())
                .eq("schedule_id", param.getScheduleId())
                .in("appointment_status", "pending", "booked");
        Long appointmentCount = appointmentMapper.selectCount(appointmentCheck);
        if (appointmentCount > 0) {
            throw new RuntimeException("您已预约该排班");
        }

        // 6. 验证候补队列是否已满（最多5人）
        QueryWrapper<Waitlist> queueLimitCheck = new QueryWrapper<>();
        queueLimitCheck.eq("schedule_id", param.getScheduleId())
                .eq("status", "waiting");
        Long currentQueueSize = waitlistMapper.selectCount(queueLimitCheck);
        if (currentQueueSize >= 5) {
            throw new RuntimeException("该排班的候补队列已满（最多5人），暂时无法加入");
        }

        // 7. 验证优先级参数
        if (param.getPriority() == null) {
            param.setPriority(0); // 默认普通优先级
        }
        if (param.getPriority() < 0 || param.getPriority() > 2) {
            throw new RuntimeException("优先级参数错误，应为0-2之间");
        }

        // 8. 创建候补记录
        Waitlist waitlist = new Waitlist();
        waitlist.setScheduleId(param.getScheduleId());
        waitlist.setPatientId(Long.valueOf(param.getPatientId()));
        waitlist.setPriority(param.getPriority());
        waitlist.setStatus("waiting");
        waitlist.setRequestedAt(LocalDateTime.now());

        // 9. 计算队列位置
        QueryWrapper<Waitlist> queueWrapper = new QueryWrapper<>();
        queueWrapper.eq("schedule_id", param.getScheduleId())
                .eq("status", "waiting")
                .orderByDesc("priority")   // 优先级高的在前
                .orderByAsc("requested_at"); // 先申请的在前
        List<Waitlist> waitlists = waitlistMapper.selectList(queueWrapper);

        int queuePosition = 1;
        queuePosition += waitlists.stream().takeWhile(w -> !w.getWaitId().equals(waitlist.getWaitId())).count();


        int result = waitlistMapper.insert(waitlist);
        if (result <= 0) {
            throw new RuntimeException("候补创建失败");
        }

        //通知
        int finalQueuePosition = queuePosition;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                notificationEmailService.sendWaitlistCreatedNotification(
                        Long.valueOf(param.getPatientId()),
                        param.getScheduleId(),
                        finalQueuePosition
                );
            }
        });

        return waitlist;
    }

    /**
     * 取消候补
     */
    @Override
    @Transactional
    public boolean cancelWaitlist(Long waitId, Long patientId) {
        Waitlist waitlist = waitlistMapper.selectById(waitId);
        if (waitlist == null || !waitlist.getPatientId().equals(patientId)) {
            return false;
        }

        if (!"waiting".equals(waitlist.getStatus())) {
            throw new RuntimeException("该候补状态不允许取消");
        }

        waitlist.setStatus("cancelled");
        return waitlistMapper.updateById(waitlist) > 0;
    }

    /**
     * 获取患者的候补列表
     */
    @Override
    public List<Waitlist> getPatientWaitlists(Long patientId) {
        return waitlistMapper.selectByPatientId(patientId);
    }

    /**
     * 处理候补队列自动转正
     * 当有号源释放时（取消预约/加号）调用此方法
     */
    @Override
    @Transactional
    public void processWaitlistConversion(Integer scheduleId) {
        // 1. 获取排班信息
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getAvailableSlots() <= 0) {
            return; // 没有可用号源
        }

        // 2. 获取候补队列（按优先级降序、请求时间升序）
        List<Waitlist> waitingList = waitlistMapper.selectWaitingListByScheduleId(scheduleId);
        if (waitingList.isEmpty()) {
            return; // 没有候补
        }

        // 3. 逐个处理候补转正，直到号源用完或候补处理完
        for (Waitlist waitlist : waitingList) {
            // 重新检查号源（因为每次转正都会消耗一个号源）
            schedule = scheduleMapper.selectById(scheduleId);
            if (schedule.getAvailableSlots() <= 0) {
                break; // 号源已用完
            }

            try {
                // 4. 自动创建预约订单
                AppointmentCreateParam appointmentParam = new AppointmentCreateParam();
                appointmentParam.setPatientId(waitlist.getPatientId());
                appointmentParam.setScheduleId(scheduleId);
                appointmentParam.setNotes("候补自动转正");

                // 调用预约服务创建预约（会自动减少号源）
                Appointment appointment = appointmentService.createAppointmentByPatient(appointmentParam);

                // 5. 更新候补状态
                waitlist.setStatus("converted");
                waitlist.setNotifiedAt(LocalDateTime.now());
                waitlist.setConvertedAppointmentId(appointment.getAppointmentId());
                waitlistMapper.updateById(waitlist);

                // 6. 发送转正通知
                notificationEmailService.sendWaitlistConversionNotification(
                        waitlist.getPatientId(),
                        appointment.getAppointmentId(),
                        scheduleId
                );

                System.out.println("候补转正成功: waitId=" + waitlist.getWaitId() +
                        ", appointmentId=" + appointment.getAppointmentId() +
                        ", patientId=" + waitlist.getPatientId());

            } catch (Exception e) {
                // 如果某个候补转正失败，记录日志并继续处理下一个
                System.err.println("候补转正失败: waitId=" + waitlist.getWaitId() +
                        ", patientId=" + waitlist.getPatientId() +
                        ", error=" + e.getMessage());
                e.printStackTrace();
                // 继续处理下一个候补
            }
        }
    }

    /**
     * 检查某个排班是否有候补队列
     */
    @Override
    public boolean hasWaitlist(Integer scheduleId) {
        QueryWrapper<Waitlist> wrapper = new QueryWrapper<>();
        wrapper.eq("schedule_id", scheduleId)
                .eq("status", "waiting");
        return waitlistMapper.selectCount(wrapper) > 0;
    }
}