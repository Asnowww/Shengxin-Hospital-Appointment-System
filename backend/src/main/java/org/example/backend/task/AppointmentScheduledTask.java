package org.example.backend.task;

import jakarta.annotation.Resource;
import org.example.backend.mapper.AppointmentMapper;
import org.example.backend.mapper.ScheduleMapper;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.Schedule;
import org.example.backend.service.NotificationEmailService;
import org.example.backend.service.WaitlistService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约订单定时任务
 * 包括：过期订单处理 和 就诊提醒通知
 */
@Component
public class AppointmentScheduledTask {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    @Lazy
    private WaitlistService waitlistService;

    @Resource
    private NotificationEmailService notificationEmailService;

    /**
     * 定时任务1：每分钟执行一次
     * 检查并处理过期的未支付订单（30分钟未支付自动过期）
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟的第0秒执行
    @Transactional
    public void handleExpiredAppointments() {
        try {
            // 查询所有过期的未支付订单
            List<Appointment> expiredAppointments = appointmentMapper.selectExpiredUnpaidAppointments(LocalDateTime.now());

            if (expiredAppointments.isEmpty()) {
                return;
            }

            System.out.println("发现 " + expiredAppointments.size() + " 个过期未支付订单，开始处理...");

            for (Appointment appointment : expiredAppointments) {
                try {
                    // 更新订单状态为已过期
                    appointment.setAppointmentStatus("cancelled");
                    appointment.setNotes("系统自动取消（超时未支付）");
                    appointment.setUpdatedAt(LocalDateTime.now());
                    appointmentMapper.updateById(appointment);

                    // 恢复排班号源
                    Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
                    if (schedule != null) {
                        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
                        schedule.setUpdatedAt(LocalDateTime.now());
                        scheduleMapper.updateById(schedule);

                        // 处理候补队列
                        try {
                            waitlistService.processWaitlistConversion(schedule.getScheduleId());
                        } catch (Exception e) {
                            System.err.println("处理候补队列失败: " + e.getMessage());
                        }
                    }

                    // 发送过期通知邮件（异步）
                    try {
                        notificationEmailService.sendAppointmentExpiredNotification(appointment.getAppointmentId());
                    } catch (Exception e) {
                        System.err.println("发送过期通知邮件失败: " + e.getMessage());
                    }

                    System.out.println("订单 " + appointment.getAppointmentId() + " 已标记为过期");

                } catch (Exception e) {
                    System.err.println("处理过期订单失败 [ID: " + appointment.getAppointmentId() + "]: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("过期订单定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务2：每天下午3点执行
     * 发送当日就诊提醒通知（已支付待就诊的订单）
     */
    @Scheduled(cron = "0 0 15 * * ?") // 每天下午3点执行
    public void sendAppointmentReminders() {
        try {
            LocalDate tommorrow = LocalDate.now().plusDays(1);

            // 查询当日所有已支付待就诊的预约
            List<Appointment> todayAppointments = appointmentMapper.selectTodayPaidAppointments(tommorrow);

            if (todayAppointments.isEmpty()) {
                System.out.println("今日无需发送就诊提醒");
                return;
            }

            System.out.println("今日有 " + todayAppointments.size() + " 个预约需要发送就诊提醒");

            for (Appointment appointment : todayAppointments) {
                try {
                    // 发送就诊提醒邮件
                    notificationEmailService.sendAppointmentReminderNotification(appointment.getAppointmentId());
                    System.out.println("已发送就诊提醒 [预约ID: " + appointment.getAppointmentId() + "]");

                } catch (Exception e) {
                    System.err.println("发送就诊提醒失败 [预约ID: " + appointment.getAppointmentId() + "]: " + e.getMessage());
                }
            }

            System.out.println("就诊提醒发送完成");

        } catch (Exception e) {
            System.err.println("就诊提醒定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}