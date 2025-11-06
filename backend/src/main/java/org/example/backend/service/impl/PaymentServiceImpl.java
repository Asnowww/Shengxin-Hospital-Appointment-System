package org.example.backend.service.impl;

import org.example.backend.mapper.AppointmentMapper;
import org.example.backend.mapper.PaymentMapper;
import org.example.backend.mapper.RefundMapper;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.Payments;
import org.example.backend.pojo.Refunds;
import org.example.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * 创建支付记录
     */
    @Override
    @Transactional
    public Payments createPayment(Long appointmentId, Double amount, String method) {
        Payments payment = new Payments();
        payment.setAppointmentId(appointmentId);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setMethod(method);
        payment.setStatus("initiated");
        payment.setCreatedAt(LocalDateTime.now());

        paymentMapper.insert(payment);

        return payment;
    }

    /**
     * 标记支付成功
     */
    @Override
    @Transactional
    public boolean markPaymentSuccess(Long paymentId, String tradeNo) {
        Payments payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            return false;
        }

        payment.setStatus("success");
        payment.setTradeNo(tradeNo);
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        // 同步更新挂号记录的支付状态
        Appointment appointment = appointmentMapper.selectById(payment.getAppointmentId());
        if (appointment != null) {
            appointment.setPaymentStatus("paid");
            appointment.setAppointmentStatus("booked"); // 已支付即视为挂号成功
            appointmentMapper.updateById(appointment);
        }

        return true;
    }

    /**
     * 标记支付失败
     */
    @Override
    @Transactional
    public boolean markPaymentFailed(Long paymentId) {
        Payments payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            return false;
        }

        payment.setStatus("failed");
        paymentMapper.updateById(payment);

        // 同步更新挂号状态
        Appointment appointment = appointmentMapper.selectById(payment.getAppointmentId());
        if (appointment != null) {
            appointment.setPaymentStatus("failed");
            appointment.setAppointmentStatus("cancelled");
            appointmentMapper.updateById(appointment);
        }

        return true;
    }

    /**
     * 创建退款记录
     */
    @Override
    @Transactional
    public Refunds createRefund(Long appointmentId, String reason) {
        // 获取支付记录
        Payments payment = paymentMapper.selectByAppointmentId(appointmentId);
        if (payment == null || !"success".equals(payment.getStatus())) {
            throw new RuntimeException("该预约没有成功支付记录，无法退款");
        }

        Refunds refund = new Refunds();
        refund.setAppointmentId(appointmentId);
        refund.setPaymentId(payment.getPaymentId());
        refund.setAmount(payment.getAmount());
        refund.setReason(reason);
        refund.setStatus("initiated");
        refund.setCreatedAt(LocalDateTime.now());

        refundMapper.insert(refund);

        return refund;
    }

    /**
     * 根据预约ID查询支付记录
     */
    @Override
    public Payments getPaymentByAppointmentId(Long appointmentId) {
        return paymentMapper.selectByAppointmentId(appointmentId);
    }

    /**
     * 处理退款成功
     */
    @Override
    @Transactional
    public boolean processRefundSuccess(Long refundId) {
        Refunds refund = refundMapper.selectById(refundId);
        if (refund == null) {
            return false;
        }

        refund.setStatus("success");
        refund.setProcessedAt(LocalDateTime.now());
        refundMapper.updateById(refund);

        // 更新支付状态
        Payments payment = paymentMapper.selectById(refund.getPaymentId());
        if (payment != null) {
            payment.setStatus("refunded");
            paymentMapper.updateById(payment);
        }

        // 更新挂号状态
        Appointment appointment = appointmentMapper.selectById(refund.getAppointmentId());
        if (appointment != null) {
            appointment.setPaymentStatus("refunded");
            appointment.setAppointmentStatus("cancelled");
            appointmentMapper.updateById(appointment);
        }

        return true;
    }
}
