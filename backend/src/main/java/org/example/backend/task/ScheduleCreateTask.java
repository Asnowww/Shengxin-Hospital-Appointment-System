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
    @Scheduled(cron = "0 00 8 ? * Fri")
    @Transactional
    public void createSchedule() {
        System.out.println("=== 开始自动创建排班任务 ===");

        // 计算下周的日期范围（周一到周日）
        LocalDate nextMonday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);
        LocalDate nextSunday = nextMonday.plusDays(6);

        System.out.println("创建排班周期: " + nextMonday + " 至 " + nextSunday);

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

        // 获取科室诊室
        ConsultationRoom room = getDeptRoom(deptId);
        if (room == null) {
            System.err.println("    " + deptName + " 没有可用诊室，跳过");
            return;
        }

        // 获取工作日
        List<LocalDate> weekdays = getWeekdays(startDate, endDate);

        // 检查上周是否有专家号/特需号
        boolean lastWeekHadExpertOrSpecial = checkLastWeekHadExpertOrSpecial(deptId, startDate);

        // 如果有专家或副主任医师，且上周没有专家号/特需号，本周强制安排
        boolean forceExpertSchedule = !expertDoctors.isEmpty() && !lastWeekHadExpertOrSpecial;

        // 选择专家号和特需号的日期
        LocalDate expertDay = null;
        LocalDate specialDay = null;

        if (!expertDoctors.isEmpty()) {
            // 随机选一天：专家号（仅上午）
            expertDay = randomSelectDays(weekdays, 1).get(0);

            // 如果有主任医师，再随机选一天作为特需号
            if (!chiefDoctors.isEmpty()) {
                do {
                    specialDay = randomSelectDays(weekdays, 1).get(0);
                } while (specialDay.equals(expertDay));
            } else if (forceExpertSchedule) {
                // 如果没有主任医师但上周没有专家号/特需号，至少要有专家号
                System.out.println("    本周强制安排专家号（上周无专家号/特需号）");
            }

            System.out.println("    专家号上午日期: " + expertDay);
            if (specialDay != null) {
                System.out.println("    特需号上午日期: " + specialDay);
            }
        } else if (forceExpertSchedule) {
            System.out.println("    警告: " + deptName + " 没有专家级别医生，无法安排专家号/特需号");
        }

        // 医生最后排班日期
        Map<Long, LocalDate> doctorLastScheduleDate = new HashMap<>();

        // 每个班次需要的医生数
        int morningDoctorCount = getDoctorCountByDept(parentDeptName, deptName);
        int afternoonDoctorCount = morningDoctorCount;
        int eveningDoctorCount = hasEvening ? 1 : 0;

        // 创建一周的排班
        for (LocalDate date : weekdays) {

            // -------- 上午排班 --------
            Integer morningType;
            List<Doctor> morningAvailableDoctors;
            int morningDoctorCountForThisDay = morningDoctorCount;

            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY);

            if (isWeekend) {
                morningType = NORMAL_TYPE;
                morningAvailableDoctors = allDoctors;
            } else if (specialDay != null && date.equals(specialDay)) {  // 特需号，仅上午，1个医生
                morningType = SPECIAL_TYPE;
                morningAvailableDoctors = chiefDoctors.isEmpty()
                        ? expertDoctors
                        : chiefDoctors;
                morningDoctorCountForThisDay = 1;  // 特需号只需要1个医生
            } else if (expertDay != null && date.equals(expertDay)) {   // 专家号，仅上午，1个医生
                morningType = EXPERT_TYPE;
                morningAvailableDoctors = expertDoctors.isEmpty()
                        ? allDoctors
                        : expertDoctors;
                morningDoctorCountForThisDay = 1;  // 专家号只需要1个医生
            } else {  // 其他工作日正常
                morningType = NORMAL_TYPE;
                morningAvailableDoctors = allDoctors;
            }

            createTimeSlotSchedules(
                    deptId, deptName, room.getRoomId(), date,
                    MORNING, morningType, morningDoctorCountForThisDay,
                    morningAvailableDoctors, doctorLastScheduleDate
            );

            // -------- 下午排班：永远普通号 --------
            createTimeSlotSchedules(
                    deptId, deptName, room.getRoomId(), date,
                    AFTERNOON, NORMAL_TYPE, afternoonDoctorCount,
                    allDoctors, doctorLastScheduleDate
            );

            // -------- 晚上（如有）：永远普通号 --------
            if (hasEvening) {
                createTimeSlotSchedules(
                        deptId, deptName, room.getRoomId(), date,
                        EVENING, NORMAL_TYPE, eveningDoctorCount,
                        allDoctors, doctorLastScheduleDate
                );
            }
        }
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
     * 为特定时间段创建排班
     */
    private void createTimeSlotSchedules(Integer deptId, String deptName, Integer roomId,
                                         LocalDate date, Integer timeSlot, Integer appointmentTypeId,
                                         int doctorCount, List<Doctor> availableDoctors,
                                         Map<Long, LocalDate> doctorLastScheduleDate) {
        // 过滤出可用医生（三天内未值班）
        List<Doctor> eligibleDoctors = availableDoctors.stream()
                .filter(doctor -> canScheduleDoctor(doctor, date, doctorLastScheduleDate))
                .collect(Collectors.toList());

        if (eligibleDoctors.size() < doctorCount) {
            // 如果符合三天规则的医生不够，放宽到所有可用医生
            eligibleDoctors = new ArrayList<>(availableDoctors);
            if (eligibleDoctors.size() < doctorCount) {
                System.out.println("    警告: " + deptName + " " + date + " " + getTimeSlotName(timeSlot) +
                        " 医生总数不足 (需要" + doctorCount + "个，实际" + eligibleDoctors.size() + "个)");
            }
        }

        // 随机选择医生
        Collections.shuffle(eligibleDoctors);
        List<Doctor> selectedDoctors = eligibleDoctors.stream()
                .limit(doctorCount)
                .collect(Collectors.toList());

        // 为每个医生创建排班
        for (Doctor doctor : selectedDoctors) {
            try {
                ScheduleCreateParam param = new ScheduleCreateParam();
                param.setDoctorId(doctor.getDoctorId());
                param.setDeptId(deptId);
                param.setRoomId(roomId);
                param.setDate(date);
                param.setTimeSlots(Collections.singletonList(timeSlot));
                param.setAppointmentTypeId(appointmentTypeId);
                param.setMaxSlots(getMaxSlotsByType(appointmentTypeId));

                scheduleService.createSchedules(param);

                // 更新医生最后值班日期
                doctorLastScheduleDate.put(doctor.getDoctorId(), date);

            } catch (Exception e) {
                // 如果是重复排班错误，不打印（正常情况）
                if (!e.getMessage().contains("已有排班")) {
                    System.err.println("    创建排班失败: " + e.getMessage());
                }
            }
        }
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
     * 获取科室的诊室
     */
    private ConsultationRoom getDeptRoom(Integer deptId) {
        QueryWrapper<ConsultationRoom> wrapper = new QueryWrapper<>();
        wrapper.eq("dept_id", deptId)
                .last("LIMIT 1");
        return consultationRoomMapper.selectOne(wrapper);
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
}