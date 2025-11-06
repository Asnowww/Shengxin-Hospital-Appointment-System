package org.example.backend.service;

import org.example.backend.dto.RoomVO;
import java.util.List;

public interface ConsultationRoomService {
    List<RoomVO> getAllRoomsWithDeptName();
    List<RoomVO> getRoomsByDept(Integer deptId);
}
