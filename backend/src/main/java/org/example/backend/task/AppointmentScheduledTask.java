package org.example.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.mapper.*;
import org.example.backend.pojo.*;
import org.example.backend.service.BookingWarningService;
import org.example.backend.service.NotificationEmailService;
import org.example.backend.service.UserBanService;
import org.example.backend.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private WaitlistMapper waitlistMapper;


    @Resource
    private NotificationEmailService notificationEmailService;

    @Resource
    private BookingWarningService bookingWarningService;  // 使用 Service

    @Autowired
    private UserMapper userMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private BookingWarningMapper bookingWarningMapper;

    @Resource
    private UserBanService userBanService;

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

    /**
     * 定时任务3：每分钟执行一次
     * 更新过期候补状态
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void handleExpiredWaitlistSimple() {
        System.out.println("执行过期候补检查...");

        List<Waitlist> expiredWaitlists = waitlistMapper.selectExpiredWaitlistRecords();

        if (expiredWaitlists.isEmpty()) {
            System.out.println("没有过期候补");
            return;
        }

        System.out.println("找到 " + expiredWaitlists.size() + " 个过期候补");

        for (Waitlist waitlist : expiredWaitlists) {
            if (!"waiting".equals(waitlist.getStatus())) {
                continue; // 跳过非waiting状态的记录
            }

            try {
                waitlist.setStatus("failed");
                waitlistMapper.updateById(waitlist);
                Schedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
                notificationEmailService.sendWaitlistFailedNotification(waitlist.getPatientId(),
                        schedule);
                System.out.println("候补 " + waitlist.getWaitId() + " 已设为failed");
            } catch (Exception e) {
                System.err.println("候补 " + waitlist.getWaitId() + " 更新失败: " + e.getMessage());
            }
        }

        System.out.println("处理完成");
    }

    /**
     * 定时任务4：每5分钟执行一次
     * 检查所有有号源的排班，自动处理候补转正
     */
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    @Transactional
    public void processAvailableSlotsWaitlist() {
        try {
            System.out.println("开始检查有号源的排班进行候补转正...");

            // 查询所有有号源的排班（可用号源 > 0）
            List<Schedule> availableSchedules = scheduleMapper.selectSchedulesWithAvailableSlots();

            if (availableSchedules.isEmpty()) {
                System.out.println("当前没有可用号源的排班");
                return;
            }

            System.out.println("发现 " + availableSchedules.size() + " 个排班有可用号源，开始处理候补转正...");

            int totalProcessed = 0;
            for (Schedule schedule : availableSchedules) {
                try {
                    // 检查该排班是否有等待中的候补
                    List<Waitlist> waitingList = waitlistMapper.selectWaitingListByScheduleId(schedule.getScheduleId());

                    if (!waitingList.isEmpty()) {
                        System.out.println("排班 [ID: " + schedule.getScheduleId() +
                                "] 有 " + schedule.getAvailableSlots() + " 个可用号源，" +
                                waitingList.size() + " 个等待候补，开始转正处理...");

                        // 调用候补转正服务
                        waitlistService.processWaitlistConversion(schedule.getScheduleId());
                        totalProcessed++;
                    }
                } catch (Exception e) {
                    System.err.println("处理排班 [ID: " + schedule.getScheduleId() + "] 候补转正失败: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理下一个排班
                }
            }

            System.out.println("候补转正定时任务完成，共处理 " + totalProcessed + " 个排班");

        } catch (Exception e) {
            System.err.println("候补转正定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务5：
     * 检查24小时内收到3次或以上警告的用户，将其状态设置为 disabled
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void handleFrequentBookingViolations() {
        try {
            System.out.println("开始检查频繁抢号违规用户...");

            // 计算24小时前的时间点
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

            // 查询所有患者
            List<Patient> allPatients = patientMapper.selectList(null);

            int rejectedCount = 0;

            for (Patient patient : allPatients) {
                try {
                    // 跳过已被禁用的用户
                    User user = userMapper.selectById(patient.getUserId());
                    if (user != null && "'disabled".equals(user.getBookingStatus())) {
                        continue;
                    }

                    // 统计该患者在24小时内的警告次数
                    Long warningCount = bookingWarningService.countWarningsSince(
                            patient.getPatientId(),
                            last24Hours
                    );

                    // 如果警告次数 >= 3次，限制该用户
                    if (warningCount != null && warningCount >= 3) {
                        // 更新用户预约状态为 'disabled
                        rejectedCount++;

                        // 写入数据库：封禁用户
                        String reason = "恶意抢号，禁止预约一年";
                        userBanService.banUser(
                                patient.getPatientId(),      // 用户ID
                                "frequent_booking",       // 禁用类型
                                reason,                   // 禁用原因
                                48                        // 限制48周
                        );

                    }

                } catch (Exception e) {
                    System.err.println("处理患者 [ID: " + patient.getPatientId() +
                            "] 抢号警告失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (rejectedCount > 0) {
                System.out.println("频繁抢号检查完成，共限制 " + rejectedCount + " 个用户");
            } else {
                System.out.println("频繁抢号检查完成，无违规用户");
            }

        } catch (Exception e) {
            System.err.println("频繁抢号检查定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务6：每天凌晨2点执行
     * 清理7天前的警告记录（可选，保持数据库清洁）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Transactional
    public void cleanupOldWarnings() {
        try {
            System.out.println("开始清理过期警告记录...");

            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            QueryWrapper<BookingWarning> wrapper = new QueryWrapper<>();
            wrapper.lt("warning_time", sevenDaysAgo);

            int deletedCount = bookingWarningMapper.delete(wrapper);

            System.out.println("已清理 " + deletedCount + " 条7天前的警告记录");

        } catch (Exception e) {
            System.err.println("清理警告记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务7：每分钟执行一次
     * 更新未就诊预约状态
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void handleExpiredAppointment() {
        System.out.println("执行过期预约检查...");

        List<Appointment> expiredAppointments = appointmentMapper.selectExpiredAppointment();

        if (expiredAppointments.isEmpty()) {
            System.out.println("没有过期预约");
            return;
        }

        System.out.println("找到 " + expiredAppointments.size() + " 个过期预约");

        for (Appointment appointment : expiredAppointments) {
            if (!"booked".equals(appointment.getAppointmentStatus())) {
                continue; // 跳过非booked状态的记录
            }

            try {
                appointment.setAppointmentStatus("no_show");
                appointmentMapper.updateById(appointment);
                notificationEmailService.sendNoShowNotification(appointment.getAppointmentId());
                System.out.println("预约 " + appointment.getAppointmentId() + " 已设为no_show");
            } catch (Exception e) {
                System.err.println("预约 " + appointment.getAppointmentId() + " 更新失败: " + e.getMessage());
            }
        }

        System.out.println("处理完成");
    }



}

