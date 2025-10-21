package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctors")
public class Doctor {

    @TableId(value = "doctor_id", type = IdType.AUTO)
    private Long doctorId;

    @TableField("user_id")
    private Long userId;

    @TableField("dept_id")
    private Integer deptId;

    private String title; // 住院医师、主治医师、副主任医师、主任医师

    private String bio;

    private String status; // active, on-leave, retired

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
