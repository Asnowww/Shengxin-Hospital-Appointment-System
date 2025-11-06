package org.example.backend.pojo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    private String rejectionReason; // 拒绝理由

    /**
     * 将 UserVerification 信息与用户信息整合成 Map
     */
    public Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("verificationId", this.verificationId);
        map.put("userId", this.userId);
        map.put("identityType", this.identityType);
        map.put("status", this.status != null ? this.status.name() : null);
        map.put("docUrl", this.docUrl);
        map.put("submittedAt", this.createdAt != null ? this.createdAt.toString() : null);
        map.put("reviewedBy", this.reviewedBy);
        map.put("reviewedAt", this.reviewedAt != null ? this.reviewedAt.toString() : null);
        map.put("rejectionReason", this.rejectionReason);
        map.put("patientId",this.idNumber);

        // 用户信息
        if (user != null) {
            map.put("username", user.getUsername());
        } else {
            map.put("username", null);
        }

        return map;
    }

    public enum VerificationStatus {
        pending, approved, rejected
    }

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("status")
    private VerificationStatus status;

}
