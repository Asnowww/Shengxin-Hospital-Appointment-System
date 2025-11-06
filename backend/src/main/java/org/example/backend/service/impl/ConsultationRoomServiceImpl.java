package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomVO;
import org.example.backend.mapper.ConsultationRoomMapper;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.pojo.ConsultationRoom;
import org.example.backend.pojo.Department;
import org.example.backend.service.ConsultationRoomService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationRoomServiceImpl implements ConsultationRoomService {

    private final ConsultationRoomMapper consultationRoomMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<RoomVO> getAllRoomsWithDeptName() {
        List<ConsultationRoom> rooms = consultationRoomMapper.selectList(null);
        List<RoomVO> result = new ArrayList<>();

        for (ConsultationRoom room : rooms) {
            RoomVO vo = new RoomVO();
            vo.setRoomId(room.getRoomId());
            vo.setDeptId(room.getDeptId());
            vo.setRoomName(room.getRoomName());
            vo.setBuilding(room.getBuilding());
            vo.setFloor(room.getFloor());
            vo.setDescription(room.getDescription());

            Department dept = departmentMapper.selectById(room.getDeptId());
            vo.setDeptName(dept != null ? dept.getDeptName() : null);

            result.add(vo);
        }
        return result;
    }

    @Override
    public List<RoomVO> getRoomsByDept(Integer deptId) {
        List<ConsultationRoom> rooms = consultationRoomMapper.selectList(
                new QueryWrapper<ConsultationRoom>().eq("dept_id", deptId)
        );
        List<RoomVO> result = new ArrayList<>();

        Department dept = departmentMapper.selectById(deptId);
        String deptName = dept != null ? dept.getDeptName() : null;

        for (ConsultationRoom room : rooms) {
            RoomVO vo = new RoomVO();
            vo.setRoomId(room.getRoomId());
            vo.setDeptId(room.getDeptId());
            vo.setDeptName(deptName);
            vo.setRoomName(room.getRoomName());
            vo.setBuilding(room.getBuilding());
            vo.setFloor(room.getFloor());
            vo.setDescription(room.getDescription());
            result.add(vo);
        }
        return result;
    }
}
