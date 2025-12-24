package org.example.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 科室创建结果
 */
@Data
public class DeptCreateResult {
    private Integer deptId;           // 创建的科室ID
    private String deptName;          // 科室名称
    private Integer roomCount;        // 创建的诊室数量
    private List<Integer> roomIds;    // 创建的诊室ID列表
    private String message;           // 提示信息
}