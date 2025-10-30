package org.example.backend.pojo;

import lombok.Data;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;

@Data
@TableName("user_verifications")
public class UserVerification {

    @TableId(type = IdType.AUTO)
    private Long verificationId;

    private Long userId;

    private String identityType; // student / teacher / staff

    private String idNumber;

    private String docUrl;

    private String status; // pending / approved / rejected

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    private String rejectionReason; // 拒绝理由

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
