package org.example.backend.dto;


import lombok.Data;
import java.util.List;

@Data
public class DepartmentTreeVO {
    private Integer id;
    private String name;
    private Integer parentId;
    private Boolean available;
    private List<DepartmentTreeVO> subDepartments;
}
