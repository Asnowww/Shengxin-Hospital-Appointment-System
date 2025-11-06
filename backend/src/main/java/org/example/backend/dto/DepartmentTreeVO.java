package org.example.backend.dto;


import lombok.Data;
import java.util.List;

@Data
public class DepartmentTreeVO {
    private Integer deptId;
    private Integer parentDeptId;
    private String deptName;
    private String building;
    private Integer floor;
    private String room;
    private String description;

    private List<DepartmentTreeVO> children;
}
