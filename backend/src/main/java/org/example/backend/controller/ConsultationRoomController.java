package org.example.backend.controller;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomVO;
import org.example.backend.service.ConsultationRoomService;
import org.example.backend.service.impl.ConsultationRoomServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ConsultationRoomController {

    @Resource
    private ConsultationRoomService consultationRoomService;

    // 获取所有诊室信息（包含科室名）
    @GetMapping("/list")
    public List<RoomVO> getAllRooms() {
        return consultationRoomService.getAllRoomsWithDeptName();
    }

    // 根据deptId查询某个科室下的诊室
    @GetMapping("/dept/{deptId}")
    public List<RoomVO> getRoomsByDept(@PathVariable Integer deptId) {
        return consultationRoomService.getRoomsByDept(deptId);
    }
}
