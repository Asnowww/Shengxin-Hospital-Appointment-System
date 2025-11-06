package org.example.backend.dto;

import lombok.Data;

@Data
public class RoomVO {
    private Integer roomId;
    private Integer deptId;
    private String deptName;
    private String roomName;
    private String building;
    private Integer floor;
    private String description;
}
