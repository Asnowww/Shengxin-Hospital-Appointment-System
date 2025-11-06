package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payments")
public class Payments {

    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long paymentId; // 支付记录ID

    private Long appointmentId; // 关联的预约ID

    private BigDecimal amount; // 实际支付金额

    private String method; // 支付方式：alipay / weChat / card / cash

    private String tradeNo; // 第三方交易号或订单号

    private String status; // 支付状态：initiated / success / failed

    @TableField("paid_at")
    private LocalDateTime paidAt; // 支付完成时间

    @TableField("created_at")
    private LocalDateTime createdAt; // 记录创建时间
}
