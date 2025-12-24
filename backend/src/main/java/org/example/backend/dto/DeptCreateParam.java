package org.example.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 添加科室请求参数
 */
@Data
public class DeptCreateParam {
    private Integer parentDeptId;     // 父科室ID（一级科室）
    private String deptName;          // 科室名称
    private String building;          // 楼宇名称
    private Integer floor;            // 楼层
    private String mainRoom;          // 主诊室号（科室办公室）
    private String description;       // 科室描述
    private List<String> roomNumbers; // 分配的诊室号列表（不包括主诊室）
}
