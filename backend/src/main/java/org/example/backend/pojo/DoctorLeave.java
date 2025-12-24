package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 逗号分隔的排班ID列表，便于审批时精准定位需要取消的排班
     */
    @TableField("schedule_ids")
    private String scheduleIds;

    private Integer affectedAppointmentCount;

    public List<Integer> safeScheduleIds() {
        if (scheduleIds == null || scheduleIds.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(scheduleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}
