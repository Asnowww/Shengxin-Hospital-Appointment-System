package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refunds")
public class Refunds {

    @TableId(value = "refund_id", type = IdType.AUTO)
    private Long refundId;  // 退款ID

    private Long paymentId; // 对应支付记录ID

    private Long appointmentId; // 对应预约ID

    private BigDecimal amount; // 退款金额

    private String reason; // 退款原因

    private String status; // 状态：initiated / success / failed

    @TableField("created_at")
    private LocalDateTime createdAt; // 创建时间

    @TableField("processed_at")
    private LocalDateTime processedAt; // 实际退款完成时间
}
