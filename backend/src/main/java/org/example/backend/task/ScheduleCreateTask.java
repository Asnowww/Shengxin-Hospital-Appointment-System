package org.example.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.ScheduleCreateParam;
import org.example.backend.mapper.ConsultationRoomMapper;
import org.example.backend.mapper.DepartmentMapper;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.mapper.ScheduleMapper;
import org.example.backend.pojo.ConsultationRoom;
import org.example.backend.pojo.Department;
import org.example.backend.pojo.Doctor;
import org.example.backend.pojo.Schedule;
import org.example.backend.service.ScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动创建排班任务
 * 排班规则：
 * 1. 内科各科室、外科各科室、儿科各科室：全天排班（上午、下午、晚上）
 * 2. 妇科、医学检验科：仅白天排班（上午、下午）
 * 3. 每个科室每周工作日随机两天有专家号，随机一天有特需号
 * 4. 专家号：主任医师或副主任医师（1个医生），特需号：主任医师（1个医生）
 * 5. 同一医生不可在三天内连续值班两次
 * 6. 内科/外科/儿科二级科室：白天2个医生，晚上1个医生
 * 7. 妇科：1个医生；医学检验科：1个医生
 * 8. 科室不能连续两周没有专家号/特需号
 * 9. 一个科室的多个诊室轮换使用，不同医生使用不同诊室
 */
@Component
public class ScheduleCreateTask {

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private ConsultationRoomMapper consultationRoomMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    // 号别类型ID
    private static final Integer NORMAL_TYPE = 1;    // 普通号
    private static final Integer EXPERT_TYPE = 2;    // 专家号
    private static final Integer SPECIAL_TYPE = 3;   // 特需号

    // 时间段
    private static final Integer MORNING = 0;    // 上午
    private static final Integer AFTERNOON = 1;  // 下午
    private static final Integer EVENING = 2;    // 晚上

    // 一级科室名称（需要全天排班）
    private static final Set<String> FULL_DAY_PARENT_DEPTS = Set.of("内科", "外科", "儿科");

    // 仅白天排班的一级科室
    private static final Set<String> DAY_ONLY_PARENT_DEPTS = Set.of("妇产科", "医学检验科");

    // 每周五早上8点执行，创建下周的排班
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void createSchedule() {
        System.out.println("=== 开始自动创建排班任务 ===");

        // 计算下周的日期范围（周一到周日）
        LocalDate nextMonday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);
        LocalDate nextSunday = nextMonday.plusDays(6);

        System.out.println("创建排班周期: " + nextMonday + " 至 " + nextSunday);

        if (hasSchedulesInWeek(nextMonday, nextSunday)) {
            System.out.println("该周期已存在排班，跳过创建");
            return;
        }

        try {
            // 获取所有一级科室
            List<Department> parentDepts = getParentDepartments();

            for (Department parentDept : parentDepts) {
                String parentName = parentDept.getDeptName();

                // 判断是否需要全天排班
                boolean hasEvening = FULL_DAY_PARENT_DEPTS.contains(parentName);

                // 获取该一级科室下的所有二级科室
                List<Department> subDepts = getSubDepartments(parentDept.getDeptId());

                if (subDepts.isEmpty()) {
                    System.out.println("跳过 " + parentName + "：没有二级科室");
                    continue;
                }

                System.out.println("\n>>> 开始为 " + parentName + " 创建排班（包含 " + subDepts.size() + " 个二级科室）");

                // 为每个二级科室创建排班
                for (Department subDept : subDepts) {
                    createSubDeptSchedules(parentName, subDept, nextMonday, nextSunday, hasEvening);
                }
            }

            System.out.println("\n=== 排班创建完成 ===");
        } catch (Exception e) {
            System.err.println("排班创建失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 为单个二级科室创建一周的排班
     */
    private void createSubDeptSchedules(String parentDeptName, Department subDept,
                                        LocalDate startDate, LocalDate endDate, boolean hasEvening) {
        Integer deptId = subDept.getDeptId();
        String deptName = subDept.getDeptName();

        System.out.println("  >> 创建 " + deptName + " 排班");

        // 获取该科室的所有在职医生
        List<Doctor> allDoctors = getDepartmentDoctors(deptId);
        if (allDoctors.isEmpty()) {
            System.err.println("    " + deptName + " 没有在职医生，跳过");
            return;
        }

        // 分类医生
        List<Doctor> chiefDoctors = filterByTitle(allDoctors, "主任医师");
        List<Doctor> viceChiefDoctors = filterByTitle(allDoctors, "副主任医师");
        List<Doctor> expertDoctors = new ArrayList<>();
        expertDoctors.addAll(chiefDoctors);
        expertDoctors.addAll(viceChiefDoctors);

        // 获取科室所有诊室
        List<ConsultationRoom> rooms = getDeptRooms(deptId);
        if (rooms.isEmpty()) {
            System.err.println("    " + deptName + " 没有可用诊室，跳过");
            return;
        }

        System.out.println("    科室拥有 " + rooms.size() + " 个诊室");

        // 获取工作日（周一到周日）
        List<LocalDate> allDays = getWeekdays(startDate, endDate);
        // 只有工作日才有专家号/特需号
        List<LocalDate> weekdaysOnly = allDays.stream()
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());

        // 检查上周是否有专家号/特需号
        boolean lastWeekHadExpertOrSpecial = checkLastWeekHadExpertOrSpecial(deptId, startDate);

        // 如果有专家或副主任医师，且上周没有专家号/特需号，本周强制安排
        boolean forceExpertSchedule = !expertDoctors.isEmpty() && !lastWeekHadExpertOrSpecial;

        // 选择专家号和特需号的日期
        Set<LocalDate> expertDays = new HashSet<>();
        LocalDate specialDay = null;

        if (!expertDoctors.isEmpty()) {
            // 规则3：随机选择2天作为专家号日期
            List<LocalDate> selectedExpertDays = randomSelectDays(weekdaysOnly, 2);
            expertDays.addAll(selectedExpertDays);

            // 如果有主任医师，再随机选1天作为特需号（不能和专家号重复）
            if (!chiefDoctors.isEmpty()) {
                List<LocalDate> remainingDays = weekdaysOnly.stream()
                        .filter(d -> !expertDays.contains(d))
                        .collect(Collectors.toList());
                if (!remainingDays.isEmpty()) {
                    specialDay = randomSelectDays(remainingDays, 1).get(0);
                }
            } else if (forceExpertSchedule) {
                System.out.println("    本周强制安排专家号（上周无专家号/特需号）");
            }

            System.out.println("    专家号上午日期: " + expertDays);
            if (specialDay != null) {
                System.out.println("    特需号上午日期: " + specialDay);
            }
        } else if (forceExpertSchedule) {
            System.out.println("    警告: " + deptName + " 没有专家级别医生，无法安排专家号/特需号");
        }

        // 医生最后排班日期
        Map<Long, LocalDate> doctorLastScheduleDate = new HashMap<>();

        // 诊室使用索引（用于轮换诊室）
        int roomIndex = 0;

        // 每个班次需要的医生数
        int daytimeDoctorCount = getDoctorCountByDept(parentDeptName, deptName);
        int eveningDoctorCount = hasEvening ? 1 : 0;

        // 记录本周所有排班，用于最后的合理性检查
        List<ScheduleRecord> weekSchedules = new ArrayList<>();

        // 创建一周的排班
        for (LocalDate date : allDays) {
            // *** 关键：记录当天已排班的医生（白天时段共享，晚上独立） ***
            Set<Long> todayDaytimeDoctors = new HashSet<>();

            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY);

            // -------- 上午排班 --------
            if (isWeekend) {
                // 周末只有普通号
                roomIndex = createTimeSlotSchedules(
                        deptId, deptName, rooms, roomIndex, date,
                        MORNING, NORMAL_TYPE, daytimeDoctorCount,
                        allDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                        weekSchedules
                );
            } else if (specialDay != null && date.equals(specialDay)) {
                // 特需号日期：1个主任医师看特需号
                roomIndex = createTimeSlotSchedules(
                        deptId, deptName, rooms, roomIndex, date,
                        MORNING, SPECIAL_TYPE, 1,
                        chiefDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                        weekSchedules
                );
                // 如果需要多个医生，其余看普通号
                if (daytimeDoctorCount > 1) {
                    roomIndex = createTimeSlotSchedules(
                            deptId, deptName, rooms, roomIndex, date,
                            MORNING, NORMAL_TYPE, daytimeDoctorCount - 1,
                            allDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                            weekSchedules
                    );
                }
            } else if (expertDays.contains(date)) {
                // 专家号日期：1个专家级医生看专家号
                roomIndex = createTimeSlotSchedules(
                        deptId, deptName, rooms, roomIndex, date,
                        MORNING, EXPERT_TYPE, 1,
                        expertDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                        weekSchedules
                );
                // 如果需要多个医生，其余看普通号
                if (daytimeDoctorCount > 1) {
                    roomIndex = createTimeSlotSchedules(
                            deptId, deptName, rooms, roomIndex, date,
                            MORNING, NORMAL_TYPE, daytimeDoctorCount - 1,
                            allDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                            weekSchedules
                    );
                }
            } else {
                // 其他日期：全部普通号
                roomIndex = createTimeSlotSchedules(
                        deptId, deptName, rooms, roomIndex, date,
                        MORNING, NORMAL_TYPE, daytimeDoctorCount,
                        allDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                        weekSchedules
                );
            }

            // -------- 下午排班：使用与上午不同的医生 --------
            roomIndex = createTimeSlotSchedules(
                    deptId, deptName, rooms, roomIndex, date,
                    AFTERNOON, NORMAL_TYPE, daytimeDoctorCount,
                    allDoctors, doctorLastScheduleDate, todayDaytimeDoctors,
                    weekSchedules
            );

            // -------- 晚上（如有）：可以使用任何医生，包括白天已排班的 --------
            if (hasEvening) {
                // 晚上独立排班，不受白天限制
                Set<Long> eveningDoctors = new HashSet<>();
                roomIndex = createTimeSlotSchedules(
                        deptId, deptName, rooms, roomIndex, date,
                        EVENING, NORMAL_TYPE, eveningDoctorCount,
                        allDoctors, doctorLastScheduleDate, eveningDoctors,
                        weekSchedules
                );
            }
        }

        // *** 排班完成后进行合理性检查 ***
        validateSchedules(deptName, weekSchedules, allDoctors);
    }

    /**
     * 排班记录类（用于验证）
     */
    private static class ScheduleRecord {
        Long doctorId;
        LocalDate date;
        Integer timeSlot;
        Integer appointmentTypeId;

        public ScheduleRecord(Long doctorId,LocalDate date,
                              Integer timeSlot, Integer appointmentTypeId) {
            this.doctorId = doctorId;
            this.date = date;
            this.timeSlot = timeSlot;
            this.appointmentTypeId = appointmentTypeId;
        }
    }

    /**
     * 验证排班合理性
     */
    private void validateSchedules(String deptName, List<ScheduleRecord> schedules, List<Doctor> allDoctors) {
        System.out.println("  >> 开始验证 " + deptName + " 排班合理性...");

        boolean hasError = false;

        // 检查1：同一医生同一天同一时段是否有重复排班
        Map<String, List<ScheduleRecord>> sameSlotGroup = schedules.stream()
                .collect(Collectors.groupingBy(s ->
                        s.doctorId + "_" + s.date + "_" + s.timeSlot));

        for (Map.Entry<String, List<ScheduleRecord>> entry : sameSlotGroup.entrySet()) {
            if (entry.getValue().size() > 1) {
                ScheduleRecord first = entry.getValue().get(0);
                System.err.println("错误：医生ID " + first.doctorId +
                        " 在 " + first.date + " " + getTimeSlotName(first.timeSlot) +
                        " 重复排班 " + entry.getValue().size() + " 次");
                hasError = true;
            }
        }

        // 检查2：同一医生白天（上午+下午）是否有重复排班
        Map<String, List<ScheduleRecord>> sameDayGroup = schedules.stream()
                .filter(s -> s.timeSlot != EVENING) // 只检查白天时段
                .collect(Collectors.groupingBy(s -> s.doctorId + "_" + s.date));

        for (Map.Entry<String, List<ScheduleRecord>> entry : sameDayGroup.entrySet()) {
            if (entry.getValue().size() > 1) {
                ScheduleRecord first = entry.getValue().get(0);
                System.err.println("错误：医生ID " + first.doctorId +
                        " 在 " + first.date + " 白天排班 " + entry.getValue().size() +
                        " 次（应该只排一个时段）");
                hasError = true;
            }
        }

        // 检查3：同一医生是否在3天内连续值班
        Map<Long, List<ScheduleRecord>> byDoctor = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.doctorId));

        for (Map.Entry<Long, List<ScheduleRecord>> entry : byDoctor.entrySet()) {
            List<LocalDate> dates = entry.getValue().stream()
                    .map(s -> s.date)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            for (int i = 0; i < dates.size() - 1; i++) {
                long daysBetween = dates.get(i + 1).toEpochDay() - dates.get(i).toEpochDay();
                if (daysBetween < 3) {
                    System.err.println("警告：医生ID " + entry.getKey() +
                            " 在 " + dates.get(i) + " 和 " + dates.get(i + 1) +
                            " 之间仅间隔 " + daysBetween + " 天（建议至少3天）");
                }
            }
        }

        // 检查4：统计排班数量
        System.out.println("本周共创建 " + schedules.size() + " 个排班");
        System.out.println("涉及 " + byDoctor.size() + " 位医生");

        if (!hasError) {
            System.out.println("✓ 排班验证通过，无严重错误");
        } else {
            System.err.println("✗ 排班验证失败，发现错误");
        }
    }

    /**
     * 为特定时间段创建排班（支持多诊室）
     * 返回下一个要使用的诊室索引
     *
     * @param todayScheduledDoctors 当天已排班的医生集合（用于防止重复）
     */
    private int createTimeSlotSchedules(Integer deptId, String deptName,
                                        List<ConsultationRoom> rooms, int startRoomIndex,
                                        LocalDate date, Integer timeSlot, Integer appointmentTypeId,
                                        int doctorCount, List<Doctor> availableDoctors,
                                        Map<Long, LocalDate> doctorLastScheduleDate,
                                        Set<Long> todayScheduledDoctors,
                                        List<ScheduleRecord> weekSchedules) {

        // 过滤出可用医生
        List<Doctor> eligibleDoctors = availableDoctors.stream()
                .filter(doctor -> {
                    // 检查1：三天内未值班
                    if (!canScheduleDoctor(doctor, date, doctorLastScheduleDate)) {
                        return false;
                    }
                    // 检查2：当天白天时段未排班（晚上除外）
                    if (timeSlot != EVENING && todayScheduledDoctors.contains(doctor.getDoctorId())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if (eligibleDoctors.size() < doctorCount) {
            // 如果符合规则的医生不够，放宽到只检查当天未排班
            eligibleDoctors = availableDoctors.stream()
                    .filter(doctor -> {
                        if (timeSlot != EVENING && todayScheduledDoctors.contains(doctor.getDoctorId())) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            if (eligibleDoctors.size() < doctorCount) {
                System.out.println("    警告: " + deptName + " " + date + " " + getTimeSlotName(timeSlot) +
                        " 可用医生不足 (需要" + doctorCount + "个，实际" + eligibleDoctors.size() + "个)");
                doctorCount = eligibleDoctors.size(); // 降级处理
            }
        }

        // 随机选择医生
        Collections.shuffle(eligibleDoctors);
        List<Doctor> selectedDoctors = eligibleDoctors.stream()
                .limit(doctorCount)
                .collect(Collectors.toList());

        // 当前诊室索引
        int currentRoomIndex = startRoomIndex % rooms.size();

        // 为每个医生创建排班，使用不同的诊室
        for (Doctor doctor : selectedDoctors) {
            // 获取当前诊室
            ConsultationRoom room = rooms.get(currentRoomIndex);

            // 检查是否已存在排班（防止同一医生同一时段重复排班）
            QueryWrapper<Schedule> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("doctor_id", doctor.getDoctorId())
                    .eq("work_date", date)
                    .eq("time_slot", timeSlot);
            Long existCount = scheduleMapper.selectCount(checkWrapper);

            if (existCount != null && existCount > 0) {
                // 已有排班，跳过
                System.out.println("    跳过：医生 " + doctor.getDoctorId() + " 在 " + date +
                        " " + getTimeSlotName(timeSlot) + " 已有排班");
                currentRoomIndex = (currentRoomIndex + 1) % rooms.size();
                continue;
            }

            ScheduleCreateParam param = new ScheduleCreateParam();
            param.setDoctorId(doctor.getDoctorId());
            param.setDeptId(deptId);
            param.setRoomId(room.getRoomId());
            param.setDate(date);
            param.setTimeSlots(Collections.singletonList(timeSlot));
            param.setAppointmentTypeId(appointmentTypeId);
            param.setMaxSlots(getMaxSlotsByType(appointmentTypeId));

            scheduleService.createSchedules(param);

            // 更新医生最后值班日期
            doctorLastScheduleDate.put(doctor.getDoctorId(), date);

            // *** 关键修改：记录到当天已排班集合 ***
            todayScheduledDoctors.add(doctor.getDoctorId());

            // 记录到周排班列表（用于验证）
            weekSchedules.add(new ScheduleRecord(
                    doctor.getDoctorId(),
                    date,
                    timeSlot,
                    appointmentTypeId
            ));

            // 移动到下一个诊室
            currentRoomIndex = (currentRoomIndex + 1) % rooms.size();
        }

        // 返回下一个要使用的诊室索引
        return currentRoomIndex;
    }


    /**
     * 检查上周是否有专家号或特需号
     */
    private boolean checkLastWeekHadExpertOrSpecial(Integer deptId, LocalDate currentWeekStart) {
        LocalDate lastWeekStart = currentWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);

        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.eq("dept_id", deptId)
                .ge("work_date", lastWeekStart)
                .le("work_date", lastWeekEnd)
                .in("appointment_type_id", Arrays.asList(EXPERT_TYPE, SPECIAL_TYPE));

        Long count = scheduleMapper.selectCount(wrapper);
        return count != null && count > 0;
    }


    /**
     * 根据科室确定医生数量
     */
    private int getDoctorCountByDept(String parentDeptName, String subDeptName) {
        // 妇科：1人
        if ("妇科".equals(subDeptName)) {
            return 1;
        }
        // 医学检验科下的科室：1人
        if ("医学检验科".equals(parentDeptName)) {
            return 1;
        }
        // 内科、外科、儿科的二级科室：2人
        if (FULL_DAY_PARENT_DEPTS.contains(parentDeptName)) {
            return 2;
        }
        // 默认1人
        return 1;
    }

    /**
     * 检查医生是否可以排班（三天内未值班）
     */
    private boolean canScheduleDoctor(Doctor doctor, LocalDate date, Map<Long, LocalDate> lastScheduleMap) {
        LocalDate lastDate = lastScheduleMap.get(doctor.getDoctorId());
        if (lastDate == null) {
            return true;
        }
        long daysBetween = Math.abs(date.toEpochDay() - lastDate.toEpochDay());
        return daysBetween >= 3;
    }

    /**
     * 获取所有一级科室
     */
    private List<Department> getParentDepartments() {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.isNull("parent_dept_id");
        return departmentMapper.selectList(wrapper);
    }

    /**
     * 获取指定一级科室下的所有二级科室
     */
    private List<Department> getSubDepartments(Integer parentDeptId) {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_dept_id", parentDeptId);
        return departmentMapper.selectList(wrapper);
    }

    /**
     * 获取科室的所有在职医生
     */
    private List<Doctor> getDepartmentDoctors(Integer deptId) {
        QueryWrapper<Doctor> wrapper = new QueryWrapper<>();
        wrapper.eq("dept_id", deptId)
                .eq("status", "active");
        return doctorMapper.selectList(wrapper);
    }

    /**
     * 按职称过滤医生
     */
    private List<Doctor> filterByTitle(List<Doctor> doctors, String title) {
        return doctors.stream()
                .filter(d -> title.equals(d.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * 获取科室的所有诊室
     */
    private List<ConsultationRoom> getDeptRooms(Integer deptId) {
        QueryWrapper<ConsultationRoom> wrapper = new QueryWrapper<>();
        wrapper.eq("dept_id", deptId);
        List<ConsultationRoom> allRooms = consultationRoomMapper.selectList(wrapper);

        // 过滤掉办公室，只保留诊室
        return allRooms.stream()
                .filter(room -> !room.getRoomName().contains("办公室"))
                .collect(Collectors.toList());
    }

    /**
     * 获取工作日列表
     */
    private List<LocalDate> getWeekdays(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> weekdays = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            weekdays.add(current);
            current = current.plusDays(1);
        }
        return weekdays;
    }

    /**
     * 随机选择指定数量的日期
     */
    private List<LocalDate> randomSelectDays(List<LocalDate> dates, int count) {
        if (dates.size() <= count) {
            return new ArrayList<>(dates);
        }
        List<LocalDate> shuffled = new ArrayList<>(dates);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * 根据号别类型获取最大号源数
     */
    private Integer getMaxSlotsByType(Integer appointmentTypeId) {
        if (SPECIAL_TYPE.equals(appointmentTypeId)) {
            return 2;  // 特需号
        } else if (EXPERT_TYPE.equals(appointmentTypeId)) {
            return 5;  // 专家号
        } else {
            return 10;  // 普通号
        }
    }

    /**
     * 时间段名称
     */
    private String getTimeSlotName(Integer timeSlot) {
        switch (timeSlot) {
            case 0: return "上午";
            case 1: return "下午";
            case 2: return "晚上";
            default: return "未知";
        }
    }

    /**
     * 检查指定周期是否已有排班
     */
    private boolean hasSchedulesInWeek(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        wrapper.ge("work_date", startDate)
                .le("work_date", endDate);
        Long count = scheduleMapper.selectCount(wrapper);
        return count != null && count > 0;
    }
}