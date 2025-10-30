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

    // ==================== 3. 排班变更相关邮件 ====================

    /**
     * 发送排班取消通知
     */
    public void sendScheduleCancelledNotification(Long patientId, Integer scheduleId, String reason) {
        try {
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) return;

            User user = userMapper.selectById(patient.getUserId());
            if (user == null || user.getEmail() == null) return;

            Schedule schedule = scheduleMapper.selectById(scheduleId);
            String subject = "【排班取消】您预约的排班已取消";
            String content = buildScheduleCancelledEmail(user.getName(), schedule, reason);

            sendEmailWithRecord(user.getUserId(), user.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送排班取消邮件失败: patientId={}, scheduleId={}", patientId, scheduleId, e);
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

    /**
     * 候补成功邮件模板
     */
    private String buildWaitlistCreatedEmail(String patientId, Schedule schedule, Integer queuePosition) {
        String patientName = getPatientName(patientId);
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

    /**
     * 候补转正邮件模板
     */
    private String buildWaitlistConversionEmail(String patientId, Appointment appointment, Schedule schedule) {
        String patientName = getPatientName(patientId);
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
    private String buildScheduleCancelledEmail(String patientId, Schedule schedule, String reason) {
        String patientName = getPatientName(patientId);
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
}