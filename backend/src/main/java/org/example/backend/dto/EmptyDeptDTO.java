package org.example.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 空科室信息（无医生的科室）
 */
@Data
public class EmptyDeptDTO {
    private Integer deptId;           // 科室ID
    private String deptName;          // 科室名称
    private String parentDeptName;    // 父科室名称
    private String building;          // 楼宇
    private Integer floor;            // 楼层
    private String room;              // 主诊室
    private Integer roomCount;        // 诊室数量
    private String description;       // 描述
}
