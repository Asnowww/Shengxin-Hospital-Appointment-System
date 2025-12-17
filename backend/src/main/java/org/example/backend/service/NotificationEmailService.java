package org.example.backend.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.mapper.*;
import org.example.backend.pojo.*;
import org.example.backend.util.EmailAPI;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通用邮件通知服务（带数据库记录）
 * 所有邮件发送都会记录到 notifications 表
 */
@Service
@Slf4j
public class NotificationEmailService {

    @Resource
    private EmailAPI emailAPI;

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private AppointmentMapper appointmentMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    // ==================== 核心发送方法 ====================

    /**
     * 发送邮件并记录到数据库（核心方法）
     * @param userId 用户ID
     * @param email 邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容（HTML）
     */
    @Async("emailTaskExecutor")
    @Transactional
    public void sendEmailWithRecord(Long userId, String email, String subject, String content) {
        // 1. 先记录到数据库（状态为 pending）
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setEmail(email);
        notification.setSubject(subject);
        notification.setContent(content);
        notification.setStatus("pending");
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);

        try {
            // 2. 发送邮件
            emailAPI.sendHtmlEmail(subject, content, email).get(); // 等待发送完成

            // 3. 更新状态为已发送
            notification.setStatus("sent");
            notification.setSentAt(LocalDateTime.now());
            notificationMapper.updateById(notification);

            log.info("邮件发送成功: notificationId={}, email={}, subject={}",
                    notification.getNotificationId(), email, subject);

        } catch (Exception e) {
            // 4. 发送失败，更新状态
            notification.setStatus("failed");
            notificationMapper.updateById(notification);

            log.error("邮件发送失败: notificationId={}, email={}, subject={}, error={}",
                    notification.getNotificationId(), email, subject, e.getMessage());
        }
    }

    /**
     * 发送简单文本邮件并记录
     */
    @Async("emailTaskExecutor")
    @Transactional
    public void sendSimpleEmailWithRecord(Long userId, String email, String subject, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setEmail(email);
        notification.setSubject(subject);
        notification.setContent(content);
        notification.setStatus("pending");
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);

        try {
            emailAPI.sendGeneralEmail(subject, content, email);
            notification.setStatus("sent");
            notification.setSentAt(LocalDateTime.now());
            notificationMapper.updateById(notification);
            log.info("简单邮件发送成功: notificationId={}, email={}", notification.getNotificationId(), email);
        } catch (Exception e) {
            notification.setStatus("failed");
            notificationMapper.updateById(notification);
            log.error("简单邮件发送失败: notificationId={}, email={}, error={}",
                    notification.getNotificationId(), email, e.getMessage());
        }
    }

    // ==================== 1. 预约相关邮件 ====================

    /**
     * 发送预约成功通知
     */
    public void sendAppointmentCreatedNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【预约成功】您的预约已创建成功";
            String content = buildAppointmentCreatedEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送预约成功邮件失败: appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送预约取消通知
     */
    public void sendAppointmentCancelledNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【预约取消】您的预约已取消";
            String content = buildAppointmentCancelledEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送预约取消邮件失败: appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送支付成功通知
     */
    public void sendPaymentSuccessNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【支付成功】您的预约已支付成功";
            String content = buildPaymentSuccessEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送支付成功邮件失败: appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送就诊通知
     */
    public void sendCompletedNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【已就诊】您已按时就诊";
            String content = buildCompletedEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送支付成功邮件失败: appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送过号通知
     */
    public void sendNoShowNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【已过号】您未按时就诊";
            String content = buildNoShowEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送支付成功邮件失败: appointmentId={}", appointmentId, e);
        }
    }




    /**
     * 发送订单退款通知
     */
    public void sendRefundSuccessNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【退款成功】您的预约已退款成功";
            String content = buildRefundSuccessEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送支付成功邮件失败: appointmentId={}", appointmentId, e);
        }
    }


    /**
     * 发送订单过期提醒
     */
    public void sendAppointmentExpiredNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;
            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;
            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【订单过期】您的预约订单未支付，现已过期";
            String content = buildAppointmentExpiredEmail(appointment,schedule);
            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);

        }catch (Exception e) {
            log.error("<UNK>: appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送就诊提醒
     */
    public void sendAppointmentReminderNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【就诊提醒】您明天有预约，请准时就诊";
            String content = buildAppointmentReminderEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送就诊提醒邮件失败: appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送叫号提醒
     */
    public void sendAppointmentCallNotification(Long appointmentId) {
        try {
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) return;

            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            String subject = "【叫号提醒】请及时就诊";
            String content = buildAppointmentCallEmail(appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送就诊提醒邮件失败: appointmentId={}", appointmentId, e);
        }
    }


    // ==================== 2. 候补相关邮件 ====================

    /**
     * 发送候补成功通知
     */
    public void sendWaitlistCreatedNotification(Long patientId, Integer scheduleId, Integer queuePosition) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(scheduleId);
            String subject = "【候补成功】您已成功加入候补队列";
            String content = buildWaitlistCreatedEmail(user.getName(), schedule, queuePosition);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送候补成功邮件失败: patientId={}, scheduleId={}", patientId, scheduleId, e);
        }
    }

    /**
     * 发送候补转正通知
     */
    public void sendWaitlistConversionNotification(Long patientId, Long appointmentId, Integer scheduleId) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Appointment appointment = appointmentMapper.selectById(appointmentId);
            Schedule schedule = scheduleMapper.selectById(scheduleId);
            String subject = "【候补转正】您的候补预约已成功转为正式预约";
            String content = buildWaitlistConversionEmail(user.getName(), appointment, schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送候补转正邮件失败: patientId={}, appointmentId={}", patientId, appointmentId, e);
        }
    }
    /**
     * 发送候补失败通知
     */
    public void sendWaitlistFailedNotification(Long patientId, Schedule schedule) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            String subject = "【候补失败】您的预约候补失败";
            String content = buildWaitlistFailedEmail(user.getName(), schedule);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送候补失败邮件失败: patientId={}, appointmentId={}", patientId, e);
        }
    }

    // ==================== 3. 排班变更相关邮件 ====================

    /**
     * 给患者发送挂号被重新分配到新的排班通知
     */
    public void sendAppointmentReassignNotification(Long patientId, Integer originalScheduleId, Integer newScheduleId, String reason) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule newSchedule = scheduleMapper.selectById(newScheduleId);

            String subject = "【挂号变更待确认】请尽快处理";
            String content = String.format("""
                尊敬的%s您好：

                因医生排班变更，您原预约的就诊时间已调整：

                📌 原排班：%s  
                ➡ 新排班：%s

                请登录系统进行确认或重新挂号，否则系统将在24小时后自动处理。

                变更原因：%s
                """,
                    user.getName(),
                    originalScheduleId,
                    newScheduleId,
                    reason
            );

            // 调用你已有的邮件发送机制 + 记录机制
            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);

        } catch (Exception e) {
            log.error("发送挂号变更通知失败: patientId={}, newScheduleId={}", patientId, newScheduleId, e);
        }
    }

    /**
     * 发送排班取消通知（包含记录 + 邮件发送）
     */
    public void sendScheduleCancelledNotification(Long patientId, Integer scheduleId, String reason) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(scheduleId);
            String subject = "【排班变更通知】您的预约已调整";
            String content = buildScheduleCancelledEmail(user.getName(), schedule, reason);

            // ➤ 记录到通知表
            Notification notification = new Notification();
            notification.setUserId(user.getUserId());
            notification.setEmail(user.getEmail());
            notification.setSubject(subject);
            notification.setContent(content);
            notification.setStatus("pending");
            notification.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(notification);

            // ➤ 发送邮件
            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);

            // ➤ 更新通知状态为成功
            notification.setStatus("sent");
            notification.setSentAt(LocalDateTime.now());
            notificationMapper.updateById(notification);

        } catch (Exception e) {
            log.error("发送排班变更通知失败: patientId={}, scheduleId={}", patientId, scheduleId, e);
        }
    }


    // ==================== 4. 通用邮件 ====================

    /**
     * 发送自定义邮件
     */
    public void sendCustomEmail(Long userId, String email, String subject, String htmlContent) {
        sendEmailWithRecord(userId, email, subject, htmlContent);
    }

    /**
     * 发送简单文本邮件
     */
    public void sendSimpleEmail(Long userId, String email, String subject, String content) {
        sendSimpleEmailWithRecord(userId, email, subject, content);
    }

    // ==================== 辅助方法 ====================

    private String getDoctorInfo(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) return "未知医生";
        User user = userMapper.selectById(doctor.getUserId());
        String name = user != null ? user.getName() : "未知医生";
        String title = doctor.getTitle() != null ? doctor.getTitle() : "";
        return name + " " + title;
    }

    private String getDeptName(Integer deptId) {
        Department dept = departmentMapper.selectById(deptId);
        return dept != null ? dept.getDeptName() : "未知科室";
    }

    private String getTimeSlotName(Integer timeSlot) {
        return switch (timeSlot) {
            case 0 -> "上午 (08:00-12:00)";
            case 1 -> "下午 (14:00-18:00)";
            case 2 -> "晚上 (18:00-21:00)";
            default -> "未知时段";
        };
    }

    private String getPatientName(String patientId) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) return "患者";
        User user = userMapper.selectById(patient.getUserId());
        return user != null ? user.getName() : "患者";
    }

    // ==================== HTML邮件模板 ====================

    /**
     * 预约成功邮件模板
     */
    private String buildAppointmentCreatedEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>✅ 预约成功</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>您的预约已创建成功，请在30分钟内完成支付。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #667eea; border-radius: 5px;">
                                <h3 style="color: #667eea; margin-top: 0;">📋 预约信息</h3>
                                <p><strong>预约编号：</strong><span style="color: #e74c3c;">%d</span></p>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                                <p><strong>排队号：</strong>%d号</p>
                                <p><strong>挂号费用：</strong>¥%.2f</p>
                            </div>
                            
                            <p style="color: #e74c3c; font-weight: bold;">⚠️ 重要提醒：请在30分钟内完成支付，否则预约将自动取消！</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo,
                workDate, timeSlot, appointment.getQueueNumber(), appointment.getFeeFinal());
    }

    /**
     * 预约取消邮件模板
     */
    private String buildAppointmentCancelledEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>❌ 预约已取消</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>您的预约已取消。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #f5576c; border-radius: 5px;">
                                <h3 style="color: #f5576c; margin-top: 0;">📋 预约信息</h3>
                                <p><strong>预约编号：</strong>%d</p>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                            </div>
                            
                            <p>如有疑问，请联系医院客服。</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo, workDate, timeSlot);
    }

    /**
     * 支付成功邮件模板
     */
    private String buildPaymentSuccessEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #56ab2f 0%%, #a8e063 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>💳 支付成功</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>您的预约已支付成功，请按时就诊。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #56ab2f; border-radius: 5px;">
                                <h3 style="color: #56ab2f; margin-top: 0;">📋 预约信息</h3>
                                <p><strong>预约编号：</strong>%d</p>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                                <p><strong>排队号：</strong>%d号</p>
                                <p><strong>支付金额：</strong>¥%.2f</p>
                            </div>
                            
                            <p><strong>温馨提示：</strong></p>
                            <ul>
                                <li>请提前15分钟到达医院</li>
                                <li>携带身份证和就诊卡</li>
                                <li>如需改期或取消，请提前联系</li>
                            </ul>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo,
                workDate, timeSlot, appointment.getQueueNumber(), appointment.getFeeFinal());
    }

    private String buildRefundSuccessEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String deptName = getDeptName(schedule.getDeptId());
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #56ab2f 0%%, #a8e063 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1>💰 退款成功通知</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p>尊敬的 <strong>%s</strong> 患者，您好：</p>
                        <p>您于本院的挂号预约已成功退款，相关金额已原路退回，请留意您的支付账户。</p>
                        
                        <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #56ab2f; border-radius: 5px;">
                            <h3 style="color: #56ab2f; margin-top: 0;">📋 退款详情</h3>
                            <p><strong>预约编号：</strong>%d</p>
                            <p><strong>就诊科室：</strong>%s</p>
                            <p><strong>主治医生：</strong>%s</p>
                            <p><strong>退款金额：</strong>¥%.2f</p>
                            <p><strong>状态：</strong>退款成功 ✅</p>
                        </div>
                        
                        <p>感谢您的理解与配合，如有疑问请联系医院客服。</p>
                        <p style="color: #888;">此邮件为系统自动发送，请勿回复。</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                patientName,
                appointment.getAppointmentId(),
                deptName,
                doctorInfo,
                appointment.getFeeFinal());
    }


    private String buildAppointmentExpiredEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>❌ 预约已过期</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>您的订单未在规定时间内支付，现已过期。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #f5576c; border-radius: 5px;">
                                <h3 style="color: #f5576c; margin-top: 0;">📋 预约信息</h3>
                                <p><strong>预约编号：</strong>%d</p>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                            </div>
                            
                            <p>如有疑问，请联系医院客服。</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo, workDate, timeSlot);

    }


    /**
     * 就诊提醒邮件模板
     */
    private String buildAppointmentReminderEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #fa709a 0%%, #fee140 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>⏰ 就诊提醒</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p style="font-size: 1.2em; color: #fa709a; font-weight: bold;">您明天有预约，请准时就诊！</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #fa709a; border-radius: 5px;">
                                <h3 style="color: #fa709a; margin-top: 0;">📋 预约信息</h3>
                                <p><strong>预约编号：</strong>%d</p>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                                <p><strong>排队号：</strong>%d号</p>
                            </div>
                            
                            <p><strong>就诊须知：</strong></p>
                            <ul>
                                <li>请提前15分钟到达医院</li>
                                <li>携带身份证和就诊卡</li>
                                <li>保持手机畅通，以便医院联系</li>
                            </ul>
                            
                            <p>祝您早日康复！</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo,
                workDate, timeSlot, appointment.getQueueNumber());
    }

    /*
    叫号邮件模板
     */
    private String buildAppointmentCallEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
    <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #ff512f 0%%, #f09819 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1>🔔 叫号提醒</h1>
                </div>
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
        
                    <p style="font-size: 1.3em; color: #ff512f; font-weight: bold;">
                        您的预约正在叫号，请立即前往就诊！
                    </p>
        
                    <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #ff512f; border-radius: 5px;">
                        <h3 style="color: #ff512f; margin-top: 0;">📋 当前叫号信息</h3>
                        <p><strong>预约编号：</strong>%d</p>
                        <p><strong>就诊科室：</strong>%s</p>
                        <p><strong>就诊医生：</strong>%s</p>
                        <p><strong>就诊时间：</strong>%s %s</p>
        
                    <div style="background: #fff3f3; padding: 15px; border-radius: 6px; border: 1px dashed #ff512f;">
                        <p style="color:#d63031; font-weight:bold; margin:0;">
                            ⚠ 请注意：若在叫号后 15 分钟内未完成签到或就诊，系统将自动判定为过号。
                        </p>
                    </div>
        
                    <p style="margin-top: 20px;">如已到达，请尽快前往对应诊室报到。</p>
        
                    <p>祝您就诊顺利，早日康复！</p>
                </div>
            </div>
        </body>
    </html>
    """,
                patientName,
                appointment.getAppointmentId(),
                deptName,
                doctorInfo,
                workDate,
                timeSlot
        );
    }

    /**
     * 候补成功邮件模板
     */
    private String buildWaitlistCreatedEmail(String patientName, Schedule schedule, Integer queuePosition) {
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #56ab2f 0%%, #a8e063 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>✅ 候补成功</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>您已成功加入候补队列，当有号源释放时，系统将自动为您预约。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #56ab2f; border-radius: 5px;">
                                <h3 style="color: #56ab2f; margin-top: 0;">📋 候补信息</h3>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                            </div>
                            
                            <p style="text-align: center;">
                                <span style="background: #56ab2f; color: white; padding: 10px 20px; border-radius: 5px; display: inline-block; margin: 15px 0;">您的候补位置：第 %d 位</span>
                            </p>
                            
                            <p><strong>温馨提示：</strong></p>
                            <ul>
                                <li>系统会按照优先级和候补时间自动处理</li>
                                <li>转正后会立即通过邮件和短信通知您</li>
                                <li>请保持手机畅通，及时查收通知</li>
                                <li>转正后需在30分钟内完成支付</li>
                            </ul>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, deptName, doctorInfo, workDate, timeSlot, queuePosition);
    }

    /*
    候补失败邮件模板
     */
    private String  buildWaitlistFailedEmail(String patientName, Schedule schedule) {
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
            <html>
            <body>
                <div style="padding: 20px;">
                    <h2 style="color: #f44336;">候补失败通知</h2>
                    <p>尊敬的%s，您好：</p>
                    <p>您申请的候补预约未能成功。</p>
                    <div style="background: #f5f5f5; padding: 15px; margin: 15px 0;">
                        <p><strong>科室：</strong>%s</p>
                        <p><strong>医生：</strong>%s</p>
                        <p><strong>时间：</strong>%s %s</p>
                    </div>
                    <p>该时间段没有号源释放，候补已结束。请重新预约其他时间。</p>
                </div>
            </body>
            </html>
            """,
                patientName, deptName, doctorInfo, workDate, timeSlot);
    }

    /**
     * 候补转正邮件模板
     */
    private String buildWaitlistConversionEmail(String patientName, Appointment appointment, Schedule schedule) {
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>🎉 候补转正通知</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>恭喜您！您的候补预约已成功转为正式预约。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #667eea; border-radius: 5px;">
                                <h3 style="color: #667eea; margin-top: 0;">📋 预约信息</h3>
                                <p><strong>预约编号：</strong><span style="color: #e74c3c;">%d</span></p>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>就诊时间：</strong>%s %s</p>
                                <p><strong>排队号：</strong>%d号</p>
                            </div>
                            
                            <p style="color: #e74c3c; font-weight: bold;">⚠️ 重要提醒：请在30分钟内完成支付，否则预约将自动取消！</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo,
                workDate, timeSlot, appointment.getQueueNumber());
    }

    /**
     * 排班取消邮件模板
     */
    private String buildScheduleCancelledEmail(String patientName, Schedule schedule, String reason) {
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                            <h1>⚠️ 排班取消通知</h1>
                        </div>
                        <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                            <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                            <p>非常抱歉，您预约的排班因故取消。</p>
                            
                            <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #f5576c; border-radius: 5px;">
                                <h3 style="color: #f5576c; margin-top: 0;">📋 排班信息</h3>
                                <p><strong>就诊科室：</strong>%s</p>
                                <p><strong>就诊医生：</strong>%s</p>
                                <p><strong>原定时间：</strong>%s %s</p>
                                <p><strong>取消原因：</strong>%s</p>
                            </div>
                            
                            <p>如已支付，费用将自动退回到您的账户。您可以选择其他时间段重新预约。</p>
                            <p>给您带来的不便，敬请谅解！</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                patientName, deptName, doctorInfo, workDate, timeSlot, reason != null ? reason : "医生临时有事");
    }

    //就诊通知
    private String buildCompletedEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1>✅ 就诊完成通知</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                        <p style="font-size: 1.2em; color: #4CAF50; font-weight: bold;">您的就诊已完成，感谢您的配合！</p>
                        
                        <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #4CAF50; border-radius: 5px;">
                            <h3 style="color: #4CAF50; margin-top: 0;">📋 就诊信息</h3>
                            <p><strong>就诊编号：</strong>%d</p>
                            <p><strong>就诊科室：</strong>%s</p>
                            <p><strong>就诊医生：</strong>%s</p>
                            <p><strong>就诊时间：</strong>%s %s</p>
                            <p><strong>完成时间：</strong>%s</p>
                        </div>
                        
                        <p><strong>后续建议：</strong></p>
                        <ul>
                            <li>请按照医嘱按时服药和复查</li>
                            <li>如有不适请及时复诊</li>
                            <li>保持健康的生活习惯</li>
                            <li>定期进行健康检查</li>
                        </ul>
                        
                        <div style="background: #e8f5e8; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p style="margin: 0; color: #2e7d32;"><strong>💡 温馨提示：</strong>您可以在患者端查看详细的就诊记录。</p>
                        </div>
                        
                        <p>祝您早日康复，身体健康！</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo,
                workDate, timeSlot, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    //过号通知
    private String buildNoShowEmail(Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(String.valueOf(appointment.getPatientId()));
        String doctorInfo = getDoctorInfo(schedule.getDoctorId());
        String deptName = getDeptName(schedule.getDeptId());
        String workDate = schedule.getWorkDate().format(DATE_FORMATTER);
        String timeSlot = getTimeSlotName(schedule.getTimeSlot());

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #ff6b6b 0%%, #ff8e8e 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1>⏰ 过号提醒</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p>尊敬的 <strong>%s</strong> 患者，您好！</p>
                        <p style="font-size: 1.2em; color: #ff6b6b; font-weight: bold;">您已错过本次预约就诊时间</p>
                        
                        <div style="background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #ff6b6b; border-radius: 5px;">
                            <h3 style="color: #ff6b6b; margin-top: 0;">📋 预约信息</h3>
                            <p><strong>预约编号：</strong>%d</p>
                            <p><strong>就诊科室：</strong>%s</p>
                            <p><strong>就诊医生：</strong>%s</p>
                            <p><strong>预约时间：</strong>%s %s</p>
                            <p><strong>排队号：</strong>%d号</p>
                            <p><strong>状态：</strong><span style="color: #ff6b6b; font-weight: bold;">已过号</span></p>
                        </div>
                        
                        <p><strong>后续操作建议：</strong></p>
                        <ul>
                            <li>如需继续就诊，请重新预约</li>
                            <li>您可以在患者端查看其他可预约时段</li>
                            <li>如有疑问，请联系医院客服</li>
                        </ul>
                        
                        <div style="background: #ffebee; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p style="margin: 0; color: #c62828;"><strong>⚠️ 重要提醒：</strong>多次过号可能会影响您的预约信用，请合理安排时间。</p>
                        </div>
                        
                        <p>感谢您的理解与配合！</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                patientName, appointment.getAppointmentId(), deptName, doctorInfo,
                workDate, timeSlot, appointment.getQueueNumber());
    }
}