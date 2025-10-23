package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("appointment_types")
public class AppointmentType {

    @TableId(value = "appointment_type_id", type = IdType.AUTO)
    private Integer appointmentTypeId;

    @TableField("type_key")
    private String typeKey; // 普通、专家、特需

    @TableField("type_name")
    private String typeName;

    @TableField("fee_amount")
    private BigDecimal feeAmount;

    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public BigDecimal getFee() {
        return feeAmount;
    }
}
