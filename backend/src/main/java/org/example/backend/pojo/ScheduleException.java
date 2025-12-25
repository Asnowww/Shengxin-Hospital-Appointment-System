package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("schedule_exceptions")
public class ScheduleException {

    @TableId(value = "exception_id", type = IdType.AUTO)
    private Long exceptionId;

    @TableField("schedule_id")
    private Integer scheduleId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("exception_type")
    private String exceptionType; // leave, cancel_all, partial_adjust, special_add

    @TableField("extra_slots")
    private Integer extraSlots; // 临时加号数量

    @TableField("adjusted_room_id")
    private Integer adjustedRoomId; // 调整后的诊室

    @TableField("adjusted_time_slot")
    private Integer adjustedTimeSlot; // 调整后的时间段

    @TableField("adjusted_date")
    private LocalDate adjustedDate; // 调整后的日期

    private String reason;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    private String status; // pending, approved, rejected (用于调班申请)
}
