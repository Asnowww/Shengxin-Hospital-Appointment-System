package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("appointment_relations")
public class AppointmentRelations {
    private Long id;
    private Long sourceAppointmentId;
    private Long targetAppointmentId;
    private String relationType;
    private String remark;
    private LocalDateTime createdAt;
    private Long createdBy;

}
