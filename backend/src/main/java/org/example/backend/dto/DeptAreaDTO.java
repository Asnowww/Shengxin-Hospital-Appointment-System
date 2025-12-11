package org.example.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 科室区域信息
 */
@Data
public class DeptAreaDTO {
    private String building;          // 楼宇名称
    private Integer floor;            // 楼层
    private List<String> availableRooms;  // 可用诊室号列表
    private Integer totalRooms;       // 该区域总诊室数
    private Integer usedRooms;        // 已使用诊室数
    private Integer availableCount;   // 可用诊室数
}
