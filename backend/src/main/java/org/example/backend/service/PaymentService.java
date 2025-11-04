package org.example.backend.service;

import org.example.backend.pojo.Payments;
import org.example.backend.pojo.Refunds;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付记录
     * @param appointmentId 预约ID
     * @param amount 支付金额
     * @param method 支付方式
     * @return 支付记录
     */
    Payments createPayment(Long appointmentId, Double amount, String method);

    /**
     * 更新支付状态为成功
     * @param paymentId 支付ID
     * @param tradeNo 第三方交易号
     * @return 是否成功
     */
    boolean markPaymentSuccess(Long paymentId, String tradeNo);

    /**
     * 更新支付状态为失败
     * @param paymentId 支付ID
     * @return 是否成功
     */
    boolean markPaymentFailed(Long paymentId);

    /**
     * 创建退款记录
     * @param appointmentId 预约ID
     * @param reason 退款原因
     * @return 退款记录
     */
    Refunds createRefund(Long appointmentId, String reason);

    /**
     * 根据预约ID查询支付记录
     * @param appointmentId 预约ID
     * @return 支付记录
     */
    Payments getPaymentByAppointmentId(Long appointmentId);

    /**
     * 处理退款成功
     * @param refundId 退款ID
     * @return 是否成功
     */
    boolean processRefundSuccess(Long refundId);
}