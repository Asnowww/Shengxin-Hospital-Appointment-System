package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
@TableName("consultation_rooms")
public class ConsultationRoom {

    @TableId(value = "room_id", type = IdType.AUTO)
    private Integer roomId;

    @TableField("dept_id")
    private Integer deptId;

    @TableField("room_name")
    private String roomName;

    private String building;

    private Integer floor;

    private String description;
}
