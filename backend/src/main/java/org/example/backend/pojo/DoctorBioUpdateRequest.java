package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_bio_update_request")
public class DoctorBioUpdateRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField(exist = false)
    private String doctorName;

    @TableField("old_bio")
    private String oldBio;

    @TableField("new_bio")
    private String newBio;

    private String status; // pending, approved, rejected

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

}

