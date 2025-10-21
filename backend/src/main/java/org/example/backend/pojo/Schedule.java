package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("schedules")
public class Schedule {

    @TableId(value = "schedule_id", type = IdType.AUTO)
    private Integer scheduleId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("dept_id")
    private Integer deptId;

    @TableField("room_id")
    private Integer roomId;

    @TableField("work_date")
    private LocalDate workDate;

    @TableField("time_slot")
    private Integer timeSlot; // 0=上午, 1=下午, 2=晚上

    @TableField("appointment_type_id")
    private Integer appointmentTypeId;

    @TableField("max_slots")
    private Integer maxSlots;

    @TableField("available_slots")
    private Integer availableSlots;

    private String status; // open, full, cancelled

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
