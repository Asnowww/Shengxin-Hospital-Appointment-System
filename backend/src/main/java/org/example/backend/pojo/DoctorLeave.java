package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("doctor_leaves")
public class DoctorLeave {

    @TableId(value = "leave_id", type = IdType.AUTO)
    private Integer leaveId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("from_date")
    private LocalDate fromDate;

    @TableField("to_date")
    private LocalDate toDate;

    private String reason;

    private String status; // pending, approved, rejected

    @TableField("applied_by")
    private Long appliedBy;

    @TableField("applied_at")
    private LocalDateTime appliedAt;

    @TableField("reviewed_by")
    private Long reviewedBy;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
}
