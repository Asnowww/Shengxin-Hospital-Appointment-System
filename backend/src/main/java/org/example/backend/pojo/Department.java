package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
@TableName("departments")
public class Department {

    @TableId(value = "dept_id", type = IdType.AUTO)
    private Integer deptId;

    @TableField("parent_dept_id")
    private Integer parentDeptId;

    @TableField("dept_name")
    private String deptName;

    private String building;

    private Integer floor;

    private String room;

    private String description;
}
