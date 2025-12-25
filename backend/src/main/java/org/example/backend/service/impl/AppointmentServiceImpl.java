package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.*;
import org.example.backend.mapper.*;
import org.example.backend.pojo.*;
import org.example.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.backend.dto.AppointmentInfoDTO;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private AppointmentTypeMapper appointmentTypeMapper;

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    @Lazy
    private WaitlistService waitlistService;

    @Resource
    private NotificationEmailService notificationEmailService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private AppointmentRelationsMapper appointmentRelationsMapper;

    @Resource
    private WaitlistMapper waitlistMapper;

    @Autowired
    private UserMapper userMapper;

    @Resource
    private BookingWarningService bookingWarningService;

    @Resource
    private UserBanService userBanService;

    @Resource
    private PaymentMapper paymentMapper;

    // === 病人端 ===

    @Override
    @Transactional
    public Appointment createAppointmentByPatient(AppointmentCreateParam param) {

        //检查用户状态是否被禁止
        Patient patient = patientMapper.selectById(param.getPatientId());
        if (patient == null) {
            throw new RuntimeException("患者信息不存在");
        }

        User user = userMapper.selectById(patient.getUserId());
        if (user == null) {
            throw new RuntimeException("用户信息不存在");
        }

        // 检查用户是否被禁止预约
        if ("disabled".equals(user.getBookingStatus())) {
            // 获取禁用详情
            BannedUser activeBan = userBanService.getActiveBan(user.getUserId());
            if (activeBan != null) {
                String banTypeMsg = getBanTypeMessage(activeBan.getBanType());
                throw new RuntimeException(String.format(
                        "您因%s已被限制预约功能。\n" +
                                "限制原因：%s\n" +
                                "解禁时间：%s\n" +
                                "注意：限制期间不影响到院后现场挂号。",
                        banTypeMsg,
                        activeBan.getBanReason(),
                        activeBan.getBanEndTime()
                ));
            }
            throw new RuntimeException("您的预约功能已被限制，请联系管理员");
        }

        // 检查抢号间隔（必须距离上次预约至少1分钟）
        checkBookingInterval(param.getPatientId());
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

        // 4. 验证患者当天是否已有同排班预约（防止重复挂号）
        QueryWrapper<Appointment> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("patient_id", param.getPatientId())
                .eq("schedule_id", param.getScheduleId())
                .in("appointment_status", "pending", "booked");
        Long existCount = appointmentMapper.selectCount(checkWrapper);
        if (existCount > 0) {
            throw new RuntimeException("您已预约该排班，请勿重复挂号");
        }

        // 验证患者是否已在该排班的候补队列中
        QueryWrapper<Waitlist> waitlistCheckWrapper = new QueryWrapper<>();
        waitlistCheckWrapper.eq("patient_id", param.getPatientId())
                .eq("schedule_id", param.getScheduleId())
                .in("status", "waiting"); // 等待中
        Long waitlistCount = waitlistMapper.selectCount(waitlistCheckWrapper);
        if (waitlistCount > 0) {
            throw new RuntimeException("您已在该排班的候补队列中，请勿重复操作");
        }

        //验证患者在同一时段是否已有其他预约（防止时间冲突）
        LocalDate workDate = schedule.getWorkDate();
        Integer timeSlot = schedule.getTimeSlot();

        //检查是否有同时段的预约
        QueryWrapper<Appointment> timeConflictWrapper = new QueryWrapper<>();
        timeConflictWrapper.eq("patient_id", param.getPatientId())
                .in("appointment_status", "pending", "booked")
                .exists("SELECT 1 FROM schedules s WHERE s.schedule_id = appointments.schedule_id " +
                        "AND s.work_date = {0} AND s.time_slot = {1}", workDate, timeSlot);

        Long conflictCount = appointmentMapper.selectCount(timeConflictWrapper);
        if (conflictCount > 0) {
            String timeSlotName = getTimeSlotName(timeSlot);
            throw new RuntimeException("您在" + workDate + " " + timeSlotName + "已有预约，无法重复预约同一时段");
        }

        //退号后不可预约相同排班
        QueryWrapper<Appointment> cancelWrapper = new QueryWrapper<>();
        cancelWrapper.eq("patient_id", param.getPatientId())
                .eq("schedule_id", param.getScheduleId())
                .eq("appointment_status", "cancelled");
        Long cancelCount = appointmentMapper.selectCount(cancelWrapper);
        if (cancelCount > 0) {
            throw new RuntimeException("您已取消过一次相同预约，现已禁止预约此排班");
        }


        // 5. 查询号别费用
        AppointmentType appointmentType = appointmentTypeMapper.selectById(schedule.getAppointmentTypeId());
        if (appointmentType == null) {
            throw new RuntimeException("号别类型不存在");
        }

//        // 6. 生成排队号（当前已预约数 + 1）
//        int queueNumber = schedule.getMaxSlots() - schedule.getAvailableSlots() + 1;

        // 7. 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(param.getPatientId());
        appointment.setScheduleId(param.getScheduleId());
        appointment.setDeptId(schedule.getDeptId());
        appointment.setRoomId(schedule.getRoomId());
        appointment.setAppointmentTypeId(schedule.getAppointmentTypeId());
//        appointment.setQueueNumber(queueNumber);
        appointment.setQueueNumber(0); // 占位，表示未进入排队，等支付成功后再排队

        // 设置费用
        appointment.setFeeOriginal(appointmentType.getFee());
        // 查询患者用于计算 feeFinal
        BigDecimal finalFee = computeFinalFee(appointmentType, patient);

        appointment.setFeeFinal(finalFee);

        // 设置状态
        appointment.setPaymentStatus("unpaid"); // 待支付
        appointment.setAppointmentStatus("pending"); // 待支付状态

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

        Long appointmentId = appointment.getAppointmentId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    // 检查是否已发送过候补转正通知
                    if (!hasWaitlistConversionNotification(appointmentId)) {
                        notificationEmailService.sendAppointmentCreatedNotification(appointmentId);
                    } else {
                        System.out.println("预约 " + appointmentId + " 的创建通知已存在，跳过发送");
                    }
                });
            }
        });

        return appointment;
    }

    /**
     * 检查预约间隔，防止恶意抢号
     * 规则：两次预约间隔必须大于1分钟，否则记录警告
     */
    private void checkBookingInterval(Long patientId) {
        // 查询最近一次预约时间
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", patientId)
                .orderByDesc("booking_time")
                .last("LIMIT 1");

        Appointment lastAppointment = appointmentMapper.selectOne(wrapper);

        if (lastAppointment != null) {
            LocalDateTime lastBookingTime = lastAppointment.getBookingTime();
            LocalDateTime now = LocalDateTime.now();

            // 计算时间差（秒）
            long secondsBetween = java.time.Duration.between(lastBookingTime, now).getSeconds();

            // 如果间隔小于60秒（1分钟）
            if (secondsBetween < 60) {
                // 使用独立事务记录警告（即使主事务回滚，警告也会保存）
                try {
                    bookingWarningService.recordWarning(
                            patientId,
                            "抢号间隔过短（" + secondsBetween + "秒）"
                    );
                } catch (Exception e) {
                    System.err.println("记录警告失败: " + e.getMessage());
                }

                // 统计最近24小时内的警告次数
                LocalDateTime last24Hours = now.minusHours(24);
                Long warningCount = bookingWarningService.countWarningsSince(patientId, last24Hours);

                // 抛出异常，阻止本次预约
                throw new RuntimeException("预约操作过于频繁，请稍后再试（至少间隔1分钟）。" +
                        "您在24小时内已收到 " + warningCount + " 次警告，" +
                        "累计3次警告将被限制预约功能。");
            }
        }
    }

    /**
     * 根据排班信息获取具体的就诊时间
     */
    private LocalDateTime getAppointmentDateTime(Schedule schedule) {
        LocalDate workDate = schedule.getWorkDate();
        Integer timeSlot = schedule.getTimeSlot();

        // 根据时段设置具体时间
        int hour;
        switch (timeSlot) {
            case 0: // 上午
                hour = 9;
                break;
            case 1: // 下午
                hour = 14;
                break;
            case 2: // 晚上
                hour = 18;
                break;
            default:
                hour = 9;
        }

        return workDate.atTime(hour, 0);
    }

    private String getBanTypeMessage(String banType) {
        return switch (banType) {
            case "no_show" -> "爽约次数过多";
            case "frequent_cancel" -> "频繁取消预约";
            case "frequent_booking" -> "频繁抢号";
            default -> "违反预约规则";
        };
    }

    private String getTimeSlotName(Integer timeSlot) {
        if (timeSlot == 0) {
            return "上午";
        }else if (timeSlot == 1) {
            return "下午";
        }else if (timeSlot == 2) {
            return "晚上";
        }

        return " ";
    }

    /**
     * 检查是否已发送过预约转正通知
     * @param appointmentId 预约ID
     * @return true表示已发送过，false表示未发送
     */
    private boolean hasWaitlistConversionNotification(Long appointmentId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("appointment_id", appointmentId)
                .eq("subject", "【候补转正】您的候补预约已成功转为正式预约");
        Long count = notificationMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    @Override
    public List<AppointmentInfoDTO> getAppointmentsByPatientId(Long patientId) {
        return appointmentMapper.selectAppointmentsByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByPatientIdAndDate(Long patientId, LocalDate date) {
        return appointmentMapper.selectList(
                new QueryWrapper<Appointment>()
                        .eq("patient_id", patientId)
                        .apply("DATE(appointment_date) = {0}", date));
    }

    @Override
    public Appointment getById(Long appointmentId) {
        return appointmentMapper.selectById(appointmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAppointment(Long appointmentId, Long patientId, Integer cancelType) {
        // 1. 查询预约
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 权限验证（用户取消时）
        if (cancelType != null && cancelType == 1 && patientId != null) {
            if (!appointment.getPatientId().equals(patientId)) {
                throw new RuntimeException("无权取消该预约");
            }
        }

        // 3. 重复取消校验
        if ("cancelled".equals(appointment.getAppointmentStatus()) ||
                "refunded".equals(appointment.getPaymentStatus())) {
            throw new RuntimeException("该预约已取消或已退款");
        }

        boolean wasPaid = "paid".equals(appointment.getPaymentStatus());

        // ========== 已支付 → 执行退款 ==========
        if (wasPaid) {
            try {
                String refundReason = (cancelType != null && cancelType == 2)
                        ? "系统自动执行退款"
                        : "用户取消预约自动退款";

                Refunds refund = paymentService.createRefund(appointmentId, refundReason);
                System.out.println("已创建退款记录：" + refund.getRefundId());

                // 模拟第三方退款调用
                Thread.sleep(1000);
                System.out.println("调用第三方退款接口成功，退款金额：" + refund.getAmount());

                boolean refundSuccess = paymentService.processRefundSuccess(refund.getRefundId());
                if (!refundSuccess) {
                    throw new RuntimeException("退款状态更新失败");
                }

                // 重新查询 appointment 更新状态，不覆盖 payment_status
                appointment = appointmentMapper.selectById(appointmentId);
                appointment.setAppointmentStatus("cancelled");
                appointment.setUpdatedAt(LocalDateTime.now());

                //强制取消的预约需要添加备注
                if(cancelType == 2) {
                    appointment.setNotes("系统强制取消");
                }

                appointmentMapper.updateStatusOnly(appointment);

                // 异步邮件通知
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        notificationEmailService.sendRefundSuccessNotification(appointmentId);
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("退款过程被中断，请稍后重试");
            } catch (Exception e) {
                throw new RuntimeException("退款失败，请稍后重试：" + e.getMessage());
            }

        } else {
            // 未支付直接取消
            appointment.setAppointmentStatus("cancelled");
            appointment.setPaymentStatus("unpaid");
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentMapper.updateById(appointment);
        }

        // 4. 释放号源
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
            schedule.setUpdatedAt(LocalDateTime.now());
            scheduleMapper.updateById(schedule);

            Integer scheduleId = appointment.getScheduleId();

            if (wasPaid) {
                appointmentMapper.recalculateQueueNumbers(scheduleId);
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    waitlistService.processWaitlistConversion(scheduleId);
                }
            });
        }

        return true;
    }



    @Transactional
    @Override
    public boolean updateAppointment(AppointmentUpdateParam param) {
        // 1. 验证预约是否存在且属于该患者
        Appointment oldAppointment = appointmentMapper.selectById(param.getAppointmentId());
        if (oldAppointment == null) {
            throw new RuntimeException("预约不存在");
        }

        if (!oldAppointment.getPatientId().equals(param.getPatientId())) {
            throw new RuntimeException("无权修改该预约");
        }

        // 2. 验证预约状态是否允许修改
        if (!"booked".equals(oldAppointment.getAppointmentStatus())
                && !"pending".equals(oldAppointment.getAppointmentStatus())) {
            throw new RuntimeException("当前预约状态不允许修改");
        }

        Integer oldScheduleId = oldAppointment.getScheduleId();
        boolean wasPaid = "paid".equals(oldAppointment.getPaymentStatus());

        // 3. 如果修改排班（改期改时间）- 创建新预约方式
        if (param.getNewScheduleId() != null &&
                !param.getNewScheduleId().equals(oldAppointment.getScheduleId())) {

            //限制改约次数
            Integer rescheduleCount = appointmentRelationsMapper.countRescheduleDepth(
                    oldAppointment.getAppointmentId()
            );

            if (rescheduleCount == null) {
                rescheduleCount = 0;
            }

            // 已经改约2次了，不能再改
            if (rescheduleCount >= 2) {
                throw new RuntimeException("该预约已改约" + rescheduleCount + "次，" +
                        "已达到最大改约次数限制（2次），无法继续改约");
            }

            // 3.1 查询新排班
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

            // 3.2 创建新预约
            Appointment newAppointment = new Appointment();
            newAppointment.setPatientId(oldAppointment.getPatientId());
            newAppointment.setScheduleId(param.getNewScheduleId());
            newAppointment.setDeptId(newSchedule.getDeptId());
            newAppointment.setRoomId(newSchedule.getRoomId());
            newAppointment.setAppointmentTypeId(newSchedule.getAppointmentTypeId());

            // 复制费用信息
            newAppointment.setFeeOriginal(oldAppointment.getFeeOriginal());
            newAppointment.setFeeFinal(oldAppointment.getFeeFinal());

            // 设置状态 - 继承原预约的支付状态
            newAppointment.setPaymentStatus(oldAppointment.getPaymentStatus());
            newAppointment.setAppointmentStatus(oldAppointment.getAppointmentStatus());

            // 设置队列号
            if (wasPaid) {
                int newQueueNumber = appointmentMapper.countPaidAppointments(param.getNewScheduleId()) + 1;
                newAppointment.setQueueNumber(newQueueNumber);
            } else {
                newAppointment.setQueueNumber(0);
            }

            // 设置时间
            newAppointment.setBookingTime(LocalDateTime.now());
            if ("pending".equals(oldAppointment.getAppointmentStatus())) {
                newAppointment.setExpireTime(oldAppointment.getExpireTime()); // 保留原过期时间
            }

            // 设置备注
            if (param.getNotes() != null && !param.getNotes().trim().isEmpty()) {
                newAppointment.setNotes(param.getNotes());
            } else {
                newAppointment.setNotes("由预约" + oldAppointment.getAppointmentId() + "改期而来");
            }

            newAppointment.setCreatedBy(param.getPatientId());
            newAppointment.setCreatedAt(LocalDateTime.now());
            newAppointment.setUpdatedAt(LocalDateTime.now());

            // 保存新预约
            int insertResult = appointmentMapper.insert(newAppointment);
            if (insertResult <= 0) {
                throw new RuntimeException("新预约创建失败");
            }

            // 3.3 更新旧预约状态为"已改约"
            oldAppointment.setAppointmentStatus("converted");
            oldAppointment.setNotes("已改约");
            oldAppointment.setUpdatedAt(LocalDateTime.now());
            appointmentMapper.updateById(oldAppointment);

            // 3.4 创建预约关联记录
            AppointmentRelations relation = new AppointmentRelations();
            relation.setSourceAppointmentId(oldAppointment.getAppointmentId());
            relation.setTargetAppointmentId(newAppointment.getAppointmentId());
            relation.setRelationType("MANUAL_RESCHEDULE"); // 手动改约
            relation.setRemark("患者主动改期改时间");
            relation.setCreatedAt(LocalDateTime.now());
            relation.setCreatedBy(param.getPatientId());
            appointmentRelationsMapper.insert(relation);

            // 3.5 如果已支付，更新支付记录关联到新预约
            if (wasPaid) {
                QueryWrapper<Payments> paymentWrapper = new QueryWrapper<>();
                paymentWrapper.eq("appointment_id", oldAppointment.getAppointmentId())
                        .eq("status", "success");

                List<Payments> payments = paymentMapper.selectList(paymentWrapper);
                for (Payments payment : payments) {
                    payment.setAppointmentId(newAppointment.getAppointmentId());
                    payment.setCreatedAt(LocalDateTime.now()); // 更新时间以记录变更
                    paymentMapper.updateById(payment);
                }
            }

            // 3.6 恢复原排班号源
            Schedule oldSchedule = scheduleMapper.selectById(oldScheduleId);
            if (oldSchedule != null) {
                oldSchedule.setAvailableSlots(oldSchedule.getAvailableSlots() + 1);
                oldSchedule.setUpdatedAt(LocalDateTime.now());
                scheduleMapper.updateById(oldSchedule);

                // 如果原预约已支付，需要重新计算队列号
                if (wasPaid) {
                    appointmentMapper.recalculateQueueNumbers(oldScheduleId);
                }

                // 处理候补队列自动转正
                Integer scheduleId = oldAppointment.getScheduleId();
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        waitlistService.processWaitlistConversion(scheduleId);
                    }
                });
            }

            // 3.7 减少新排班号源
            newSchedule.setAvailableSlots(newSchedule.getAvailableSlots() - 1);
            newSchedule.setUpdatedAt(LocalDateTime.now());
            scheduleMapper.updateById(newSchedule);

            // 如果新预约已支付，需要重新计算队列号
            if (wasPaid) {
                appointmentMapper.recalculateQueueNumbers(param.getNewScheduleId());
            }

            // 3.8 发送改约通知
            Long newAppointmentId = newAppointment.getAppointmentId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    CompletableFuture.runAsync(() -> {
                        notificationEmailService.sendAppointmentRescheduledNotification(
                                oldAppointment.getAppointmentId(),
                                newAppointmentId
                        );
                    });
                }
            });

            return true;
        }

        // 4. 如果只是修改备注（不改排班）
        if (param.getNotes() != null) {
            oldAppointment.setNotes(param.getNotes());
        }

        // 5. 更新支付状态和预约状态（如果有）
        if (param.getPaymentStatus() != null) {
            oldAppointment.setPaymentStatus(param.getPaymentStatus());
        }
        if (param.getAppointmentStatus() != null) {
            oldAppointment.setAppointmentStatus(param.getAppointmentStatus());
        }

        // 6. 更新时间戳
        oldAppointment.setUpdatedAt(LocalDateTime.now());

        return appointmentMapper.updateById(oldAppointment) > 0;
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

        // 检查是否在就诊前1小时内
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            LocalDateTime appointmentDateTime = getAppointmentDateTime(schedule);
            LocalDateTime now = LocalDateTime.now();

            // 如果距离就诊时间不足1小时，不允许修改
            long hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();
            if (hoursUntilAppointment < 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * 支付预约
     */
    @Transactional
    public boolean payAppointment(Long appointmentId, Long patientId, String method) {
        // 1. 查询预约记录
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("无效的预约记录或无权支付");
        }

        // 2. 验证当前状态
        if (!"unpaid".equals(appointment.getPaymentStatus())) {
            throw new RuntimeException("该预约已支付或无法重复支付");
        }

        // 3. 创建支付记录
        Payments payment = paymentService.createPayment(
                appointmentId,
                appointment.getFeeFinal().doubleValue(),
                method);

        try {
            // === 模拟调用第三方支付接口 ===
            System.out.println("正在调用第三方支付接口，支付方式：" + method);
            Thread.sleep(10); // 模拟网络请求
            String tradeNo = "TRADE-" + System.currentTimeMillis();

            // 4. 支付成功，更新支付状态
            boolean paySuccess = paymentService.markPaymentSuccess(payment.getPaymentId(), tradeNo);
            if (!paySuccess) {
                throw new RuntimeException("支付状态更新失败");
            }

            // 5. 支付成功后发送邮件（事务提交后执行）
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    notificationEmailService.sendPaymentSuccessNotification(appointment.getAppointmentId());
                }
            });

            return true;

        } catch (Exception e) {
            System.err.println("支付失败：" + e.getMessage());
            paymentService.markPaymentFailed(payment.getPaymentId());
            throw new RuntimeException("支付失败，请稍后重试");
        }
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
        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public boolean updateAppointmentFee(Integer id, Double fee) {
        AppointmentType type = appointmentTypeMapper.selectById(id);
        if (type == null)
            return false;
        type.setFeeAmount(java.math.BigDecimal.valueOf(fee));
        return appointmentTypeMapper.updateById(type) > 0;
    }

    @Override
    public List<AppointmentType> getAllAppointmentTypes() {
        return appointmentTypeMapper.selectList(null);
    }

    // ========== 统计功能方法 ==========

    /**
     * 统计每个医生在一段时间内的挂号数量
     */
    @Override
    public List<DoctorAppointmentStats> getDoctorAppointmentStats(LocalDate startDate, LocalDate endDate) {
        return appointmentMapper.selectDoctorAppointmentStats(startDate, endDate);
    }

    /**
     * 统计每个科室在一段时间内的挂号数量
     */
    @Override
    public List<DepartmentAppointmentStats> getDepartmentAppointmentStats(LocalDate startDate, LocalDate endDate) {
        return appointmentMapper.selectDepartmentAppointmentStats(startDate, endDate);
    }

    @Override
    public Result<Object> calculateFee(Long appointmentId) {

        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null)
            return Result.error("挂号记录不存在");

        AppointmentType type = appointmentTypeMapper.selectById(appointment.getAppointmentTypeId());
        if (type == null)
            return Result.error("挂号类别不存在");

        Patient patient = patientMapper.selectById(appointment.getPatientId());
        if (patient == null)
            return Result.error("患者信息不存在");

        BigDecimal finalFee = computeFinalFee(type, patient);

        appointment.setFeeFinal(finalFee);
        appointmentMapper.updateById(appointment);

        return Result.success("费用已计算", finalFee);
    }

    @Override
    public BigDecimal computeFinalFee(AppointmentType type, Patient patient) {
        BigDecimal baseFee = type.getFeeAmount();
        BigDecimal discountRate = switch (patient.getIdentityType()) {
            case "student" -> new BigDecimal("0.95");
            case "teacher" -> new BigDecimal("0.90");
            case "staff" -> new BigDecimal("0.85");
            default -> BigDecimal.ZERO;
        };

        return baseFee.multiply(BigDecimal.ONE.subtract(discountRate));
    }

    @Override
    @Transactional
    public boolean appointmentComplete(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null)
            return false;
        appointment.setAppointmentStatus("completed");
        appointment.setVisitTime(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        /* 发送已就诊通知 */
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                notificationEmailService.sendCompletedNotification(appointment.getAppointmentId());
//            }
//        });
        // ⚠️ 关键点：afterCommit + @Async = 真正不阻塞请求
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        notificationEmailService
                                .sendCompletedNotification(appointment.getAppointmentId());
                    }
                }
        );

        return true;
    }

    @Override
    @Transactional
    public boolean appointmentPass(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null)
            return false;
        appointment.setAppointmentStatus("no_show");
        appointment.setVisitTime(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        /* 发送过号通知 */
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                notificationEmailService.sendNoShowNotification(appointment.getAppointmentId());
            }
        });

        return true;
    }

    @Override
    @Transactional
    public boolean callPatient(Long appointmentId) {

        Appointment appointment = appointmentMapper.selectById(appointmentId);

        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        Appointment next = appointmentMapper.selectOne(
                new QueryWrapper<Appointment>()
                        .eq("schedule_id", appointment.getScheduleId())
                        .eq("appointment_status", "booked")
                        .orderByAsc("queue_number")
                        .last("limit 1"));

        if (!next.getAppointmentId().equals(appointmentId)) {
            throw new RuntimeException("请按顺序叫号");
        }

        // 只允许对已挂号的预约叫号
        if (!"booked".equals(appointment.getAppointmentStatus())) {
            throw new RuntimeException("当前状态不允许叫号");
        }

        appointment.setUpdatedAt(LocalDateTime.now());

        int rows = appointmentMapper.updateById(appointment);

        if (rows <= 0) {
            throw new RuntimeException("叫号更新失败");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                notificationEmailService.sendAppointmentCallNotification(appointment.getAppointmentId());
            }
        });

        startAutoNoShowTask(appointmentId);

        return true;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private void startAutoNoShowTask(Long appointmentId) {
        scheduler.schedule(() -> {

            Appointment appointment = appointmentMapper.selectById(appointmentId);

            if (appointment == null)
                return;

            // 如果还处于 booked ，说明未就诊，自动过号
            if ("booked".equals(appointment.getAppointmentStatus())) {
                appointment.setAppointmentStatus("no_show");
                appointment.setUpdatedAt(LocalDateTime.now());
                appointmentMapper.updateById(appointment);

                /* 发送过号通知 */
                notificationEmailService.sendNoShowNotification(appointment.getAppointmentId());

            }

        }, 15, TimeUnit.MINUTES);
    }

    /**
     * 获取患者所有历史就诊记录
     */
    @Override
    public List<AppointmentInfoDTO> getPatientHistory(Long patientId, Integer limit) {
        List<AppointmentInfoDTO> history = appointmentMapper.selectAppointmentsByPatientId(patientId);

        // 如果需要限制数量
        if (limit != null && limit > 0 && history.size() > limit) {
            return history.subList(0, limit);
        }

        return history;
    }

    @Override
    @Transactional
    public void confirmReassignedAppointment(Long targetAppointmentId, Long patientId) {
        Appointment target = appointmentMapper.selectById(targetAppointmentId);
        if (target == null) {
            throw new RuntimeException("新预约不存在");
        }
        if (!target.getPatientId().equals(patientId)) {
            throw new RuntimeException("无权操作该预约");
        }
        if (!"pending_patient_confirm".equals(target.getAppointmentStatus())) {
            throw new RuntimeException("该预约不需要确认");
        }

        AppointmentRelations rel = appointmentRelationsMapper.selectOne(
                new QueryWrapper<AppointmentRelations>()
                        .eq("target_appointment_id", targetAppointmentId)
                        .orderByDesc("created_at")
                        .last("LIMIT 1"));
        if (rel == null || rel.getSourceAppointmentId() == null) {
            throw new RuntimeException("未找到关联的原预约记录");
        }

        Appointment source = appointmentMapper.selectById(rel.getSourceAppointmentId());
        if (source == null) {
            throw new RuntimeException("原预约不存在");
        }
        if (!source.getPatientId().equals(patientId)) {
            throw new RuntimeException("无权操作该预约");
        }

       target.setAppointmentStatus("pending"); //TODO:是否需要？
        target.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(target);

        source.setAppointmentStatus("cancelled");
        source.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(source);
    }

    @Override
    @Transactional
    public void acknowledgeCancelledAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("无权操作该预约");
        }
        if (!"waiting_patient_action".equals(appointment.getAppointmentStatus())) {
            throw new RuntimeException("该预约无需确认");
        }

        appointment.setAppointmentStatus("cancelled");
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
    }

    @Override
    @Transactional
    public void rejectReassignedAppointment(Long targetAppointmentId, Long patientId) {
        Appointment target = appointmentMapper.selectById(targetAppointmentId);
        if (target == null) {
            throw new RuntimeException("新预约不存在");
        }
        if (!target.getPatientId().equals(patientId)) {
            throw new RuntimeException("无权操作该预约");
        }
        if (!"pending_patient_confirm".equals(target.getAppointmentStatus())) {
            throw new RuntimeException("该预约不在待确认状态");
        }

        target.setNotes("系统自动调剂预约已拒绝");
        target.setAppointmentStatus("cancelled");
        target.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(target);
    }

    @Override
    public int countAffectedAppointments(String scheduleIds) {
        if (scheduleIds == null || scheduleIds.trim().isEmpty()) {
            return 0;
        }

        // 1. 解析 scheduleIds
        List<Long> ids = Arrays.stream(scheduleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return 0;
        }

        // 2. 一条 SQL 统计所有受影响预约（避免 N+1）
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.in("schedule_id", ids)
                .in("appointment_status", "pending", "booked");

        return Math.toIntExact(appointmentMapper.selectCount(wrapper));
    }


}