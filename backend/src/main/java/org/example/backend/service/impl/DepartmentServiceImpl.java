package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.*;
import org.example.backend.mapper.ConsultationRoomMapper;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.pojo.ConsultationRoom;
import org.example.backend.pojo.Department;
import org.example.backend.service.DepartmentService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private ConsultationRoomMapper consultationRoomMapper;

    @Resource
    private DoctorMapper doctorMapper;


    /**
     * 获取可预约科室树（假设全部科室都可预约）
     */
    @Override
    public List<DepartmentTreeVO> getAvailableDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(null);
        return buildDepartmentTree(allDepartments);
    }

    /**
     * 获取所有科室树结构
     */
    @Override
    public List<DepartmentTreeVO> getAllDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(null);
        return buildDepartmentTree(allDepartments);
    }

    /**
     * 新增科室
     */
    @Override
    public void addDepartment(Department department) {
        departmentMapper.insert(department);
    }

    /**
     * 修改科室
     */
    @Override
    public void updateDepartment(Department department) {
        departmentMapper.updateById(department);
    }

    /**
     * 删除科室
     */
    @Override
    @Transactional
    public boolean deleteDepartment(Integer id) {
        // 1. 先删诊室
        consultationRoomMapper.deleteByDeptId(id);

        // 2. 再删科室
        return departmentMapper.deleteById(id) > 0;
    }

    /**
     * 获取所有可用的科室区域（按楼层分组）
     */
    @Override
    public List<DeptAreaDTO> getAvailableAreas() {
        // 获取所有已使用的诊室
        List<ConsultationRoom> usedRooms = consultationRoomMapper.selectList(null);

        // 按楼宇和楼层分组已使用的诊室
        Map<String, Set<String>> usedRoomMap = new HashMap<>();
        for (ConsultationRoom room : usedRooms) {
            String key = room.getBuilding() + "-" + room.getFloor();
            usedRoomMap.computeIfAbsent(key, k -> new HashSet<>()).add(room.getRoomName());
        }

        // 诊室编号范围：1~30（楼层房间采用 101~130, 201~230）
        int minRoomNumber = 1;
        int maxRoomNumber = 30;

        // 楼宇与楼层
        List<String> buildings = Arrays.asList("圣心楼");
        List<Integer> floors = Arrays.asList(1, 2, 3, 4, 5, 6);

        List<DeptAreaDTO> areas = new ArrayList<>();

        // 遍历所有楼宇和楼层
        for (String building : buildings) {
            for (Integer floor : floors) {
                String key = building + "-" + floor;
                Set<String> usedInArea = usedRoomMap.getOrDefault(key, new HashSet<>());

                // 生成该区域所有可能的诊室号：例如 1楼 => 101~130
                List<String> availableRooms = new ArrayList<>();
                for (int i = minRoomNumber; i <= maxRoomNumber; i++) {
                    String roomNum = String.valueOf(floor * 100 + i);
                    if (!usedInArea.contains(roomNum)) {
                        availableRooms.add(roomNum);
                    }
                }

                // 只返回存在空诊室的楼层
                if (!availableRooms.isEmpty()) {
                    DeptAreaDTO area = new DeptAreaDTO();
                    area.setBuilding(building);
                    area.setFloor(floor);
                    area.setAvailableRooms(availableRooms);
                    area.setTotalRooms(maxRoomNumber - minRoomNumber + 1);
                    area.setUsedRooms(usedInArea.size());
                    area.setAvailableCount(availableRooms.size());
                    areas.add(area);
                }
            }
        }

        return areas;
    }


    /**
     * 创建二级科室并分配诊室
     */
    @Override
    @Transactional
    public DeptCreateResult createDepartmentWithRooms(DeptCreateParam param) {
        // 1. 参数校验
        if (param.getParentDeptId() == null) {
            throw new IllegalArgumentException("必须指定父科室（一级科室）");
        }

        if (param.getDeptName() == null || param.getDeptName().trim().isEmpty()) {
            throw new IllegalArgumentException("科室名称不能为空");
        }

        if (param.getRoomNumbers() == null || param.getRoomNumbers().isEmpty()) {
            throw new IllegalArgumentException("必须至少分配一个诊室");
        }

        // 2. 检查父科室是否存在
        Department parentDept = departmentMapper.selectById(param.getParentDeptId());
        if (parentDept == null) {
            throw new IllegalArgumentException("父科室不存在");
        }

        // 3. 检查科室名称是否重复
        QueryWrapper<Department> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("dept_name", param.getDeptName());
        if (departmentMapper.selectCount(nameWrapper) > 0) {
            throw new IllegalArgumentException("科室名称已存在");
        }

        // 4. 检查诊室是否已被占用
        for (String roomNumber : param.getRoomNumbers()) {
            QueryWrapper<ConsultationRoom> roomWrapper = new QueryWrapper<>();
            roomWrapper.eq("building", param.getBuilding())
                    .eq("floor", param.getFloor())
                    .eq("room_name", roomNumber);
            if (consultationRoomMapper.selectCount(roomWrapper) > 0) {
                throw new IllegalArgumentException("诊室 " + roomNumber + " 已被占用");
            }
        }

        // 检查主诊室
        if (param.getMainRoom() != null && !param.getMainRoom().isEmpty()) {
            QueryWrapper<ConsultationRoom> mainRoomWrapper = new QueryWrapper<>();
            mainRoomWrapper.eq("building", param.getBuilding())
                    .eq("floor", param.getFloor())
                    .eq("room_name", param.getMainRoom());
            if (consultationRoomMapper.selectCount(mainRoomWrapper) > 0) {
                throw new IllegalArgumentException("主诊室 " + param.getMainRoom() + " 已被占用");
            }
        }

        // 5. 创建科室记录
        Department department = new Department();
        department.setParentDeptId(param.getParentDeptId());
        department.setDeptName(param.getDeptName());
        department.setBuilding(param.getBuilding());
        department.setFloor(param.getFloor());
        department.setRoom(param.getMainRoom() != null && !param.getMainRoom().isEmpty()
                ? param.getMainRoom()
                : param.getRoomNumbers().get(0));
        department.setDescription(param.getDescription());

        departmentMapper.insert(department);
        Integer deptId = department.getDeptId();

        // 6. 创建诊室记录
        List<Integer> roomIds = new ArrayList<>();

        // 先创建主诊室（办公室）
        if (param.getMainRoom() != null && !param.getMainRoom().isEmpty()) {
            ConsultationRoom mainRoom = new ConsultationRoom();
            mainRoom.setDeptId(deptId);
            mainRoom.setRoomName(param.getMainRoom());
            mainRoom.setBuilding(param.getBuilding());
            mainRoom.setFloor(param.getFloor());
            mainRoom.setDescription(param.getDeptName() + "办公室");
            consultationRoomMapper.insert(mainRoom);
            roomIds.add(mainRoom.getRoomId());
        }

        // 创建其他诊室
        int roomIndex = 1;
        for (String roomNumber : param.getRoomNumbers()) {
            ConsultationRoom room = new ConsultationRoom();
            room.setDeptId(deptId);
            room.setRoomName(roomNumber);
            room.setBuilding(param.getBuilding());
            room.setFloor(param.getFloor());
            room.setDescription(param.getDeptName() + "诊室" + roomIndex);
            consultationRoomMapper.insert(room);
            roomIds.add(room.getRoomId());
            roomIndex++;
        }

        // 7. 返回结果
        DeptCreateResult result = new DeptCreateResult();
        result.setDeptId(deptId);
        result.setDeptName(param.getDeptName());
        result.setRoomCount(roomIds.size());
        result.setRoomIds(roomIds);
        result.setMessage("科室创建成功，已分配 " + roomIds.size() + " 个诊室");

        return result;
    }

    /**
     * 获取所有没有医生的科室列表
     */
    @Override
    public List<EmptyDeptDTO> getEmptyDepartments() {
        // 获取所有二级科室
        QueryWrapper<Department> deptWrapper = new QueryWrapper<>();
        deptWrapper.isNotNull("parent_dept_id");
        List<Department> allSubDepts = departmentMapper.selectList(deptWrapper);

        List<EmptyDeptDTO> emptyDepts = new ArrayList<>();

        for (Department dept : allSubDepts) {
            // 查询该科室的医生数量
            QueryWrapper<Object> doctorWrapper = new QueryWrapper<>();
            doctorWrapper.eq("dept_id", dept.getDeptId())
                    .eq("status", "active");  // 只统计在职医生
            Long doctorCount = doctorMapper.selectCount((QueryWrapper) doctorWrapper);

            // 如果没有在职医生，添加到结果列表
            if (doctorCount == 0) {
                EmptyDeptDTO emptyDept = new EmptyDeptDTO();
                emptyDept.setDeptId(dept.getDeptId());
                emptyDept.setDeptName(dept.getDeptName());

                // 获取父科室名称
                Department parentDept = departmentMapper.selectById(dept.getParentDeptId());
                emptyDept.setParentDeptName(parentDept != null ? parentDept.getDeptName() : "未知");

                emptyDept.setBuilding(dept.getBuilding());
                emptyDept.setFloor(dept.getFloor());
                emptyDept.setRoom(dept.getRoom());
                emptyDept.setDescription(dept.getDescription());

                // 查询该科室的诊室数量
                QueryWrapper<ConsultationRoom> roomWrapper = new QueryWrapper<>();
                roomWrapper.eq("dept_id", dept.getDeptId());
                Long roomCount = consultationRoomMapper.selectCount(roomWrapper);
                emptyDept.setRoomCount(roomCount.intValue());

                emptyDepts.add(emptyDept);
            }
        }

        return emptyDepts;
    }

    /**
     * 获取所有一级科室列表
     */
    @Override
    public List<Department> getParentDepartments() {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.isNull("parent_dept_id");
        return departmentMapper.selectList(wrapper);
    }


    /**
     * 构建科室树（一级科室 → 二级科室）
     */
    private List<DepartmentTreeVO> buildDepartmentTree(List<Department> allDepartments) {
        List<DepartmentTreeVO> roots = allDepartments.stream()
                .filter(d -> d.getParentDeptId() == null)
                .map(this::toVO)
                .collect(Collectors.toList());

        for (DepartmentTreeVO root : roots) {
            List<DepartmentTreeVO> children = allDepartments.stream()
                    .filter(d -> root.getDeptId().equals(d.getParentDeptId()))
                    .map(this::toVO)
                    .collect(Collectors.toList());
            root.setChildren(children);
        }
        return roots;
    }

    private DepartmentTreeVO toVO(Department department) {
        DepartmentTreeVO vo = new DepartmentTreeVO();
        vo.setDeptId(department.getDeptId());
        vo.setDeptName(department.getDeptName());
        vo.setParentDeptId(department.getParentDeptId());
        vo.setBuilding(department.getBuilding());
        vo.setFloor(department.getFloor());
        vo.setRoom(department.getRoom());
        vo.setDescription(department.getDescription());
        return vo;
    }
}
