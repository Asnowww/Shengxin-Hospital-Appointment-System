package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;
    private String password;
    private String gender;
    private String phone;


    private String email;

    @TableField("role_type")
    private String roleType; // admin / doctor / patient

    @TableField("status")
    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public String getName() {
        return username;
    }
}
