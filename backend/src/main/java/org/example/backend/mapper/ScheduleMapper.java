package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.dto.DoctorAttendanceStats;
import org.example.backend.dto.DoctorWorkloadStats;
import org.example.backend.dto.ScheduleCoverageStats;
import org.example.backend.dto.ScheduleUtilizationStats;
import org.example.backend.pojo.Schedule;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
    /**
     * 统计排班利用率
     */
    @Select("<script>" +
            "SELECT " +
            "    d.doctor_id, " +
            "    u.username AS doctor_name, " +
            "    d.dept_id, " +
            "    dept.dept_name, " +
            "    SUM(s.max_slots) AS total_slots, " +
            "    SUM(s.max_slots - s.available_slots) AS booked_slots, " +
            "    ROUND(SUM(s.max_slots - s.available_slots) * 100.0 / SUM(s.max_slots), 2) AS utilization_rate " +
            "FROM schedules s " +
            "INNER JOIN doctors d ON s.doctor_id = d.doctor_id " +
            "INNER JOIN users u ON d.user_id = u.user_id " +
            "INNER JOIN departments dept ON d.dept_id = dept.dept_id " +
            "WHERE s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY d.doctor_id, u.username, d.dept_id, dept.dept_name " +
            "ORDER BY utilization_rate DESC" +
            "</script>")
    List<ScheduleUtilizationStats> selectScheduleUtilizationStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计医生工作量
     */
    @Select("<script>" +
            "SELECT " +
            "    d.doctor_id, " +
            "    u.username AS doctor_name, " +
            "    d.dept_id, " +
            "    dept.dept_name, " +
            "    COUNT(s.schedule_id) AS schedule_count, " +
            "    COUNT(a.appointment_id) AS appointment_count, " +
            "    SUM(s.max_slots) AS total_slots, " +
            "    SUM(s.available_slots) AS available_slots, " +
            "    SUM(s.max_slots - s.available_slots) AS booked_slots " +
            "FROM schedules s " +
            "INNER JOIN doctors d ON s.doctor_id = d.doctor_id " +
            "INNER JOIN users u ON d.user_id = u.user_id " +
            "INNER JOIN departments dept ON d.dept_id = dept.dept_id " +
            "LEFT JOIN appointments a ON s.schedule_id = a.schedule_id " +
            "    AND a.appointment_status IN ('booked', 'completed') " +
            "WHERE s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY d.doctor_id, u.username, d.dept_id, dept.dept_name " +
            "ORDER BY schedule_count DESC" +
            "</script>")
    List<DoctorWorkloadStats> selectDoctorWorkloadStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 按科室统计排班覆盖率
     */
    @Select("<script>" +
            "SELECT " +
            "    dept.dept_id, " +
            "    dept.dept_name, " +
            "    DATEDIFF(#{endDate}, #{startDate}) + 1 AS total_days, " +
            "    COUNT(DISTINCT s.work_date) AS scheduled_days, " +
            "    ROUND(COUNT(DISTINCT s.work_date) * 100.0 / (DATEDIFF(#{endDate}, #{startDate}) + 1), 2) AS coverage_rate " +
            "FROM departments dept " +
            "LEFT JOIN doctors d ON dept.dept_id = d.dept_id " +
            "LEFT JOIN schedules s ON d.doctor_id = s.doctor_id " +
            "   AND s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY dept.dept_id, dept.dept_name " +
            "ORDER BY coverage_rate DESC" +
            "</script>")
    List<ScheduleCoverageStats> selectScheduleCoverageStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /**
     * 统计医生出诊率
     */
    @Select("<script>" +
            "SELECT " +
            "    d.doctor_id, " +
            "    u.username AS doctor_name, " +
            "    d.dept_id, " +
            "    dept.dept_name, " +
            "    COUNT(s.schedule_id) AS scheduled_count, " +
            "    SUM(CASE WHEN s.status IN ('open', 'full') THEN 1 ELSE 0 END) AS attended_count, " +
            "    ROUND(SUM(CASE WHEN s.status IN ('open', 'full') THEN 1 ELSE 0 END) * 100.0 / COUNT(s.schedule_id), 2) AS attendance_rate " +
            "FROM schedules s " +
            "INNER JOIN doctors d ON s.doctor_id = d.doctor_id " +
            "INNER JOIN users u ON d.user_id = u.user_id " +
            "INNER JOIN departments dept ON d.dept_id = dept.dept_id " +
            "WHERE s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY d.doctor_id, u.username, d.dept_id, dept.dept_name " +
            "ORDER BY attendance_rate DESC" +
            "</script>")
    List<DoctorAttendanceStats> selectDoctorAttendanceStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询还有剩余号源且在有效时间的排班
     * @return 符合条件的排班列表
     */
    @Select("SELECT * FROM schedules " +
            "WHERE available_slots > 0 " +
            "AND status = 'open' " +
            "AND work_date >= CURDATE() " +
            "ORDER BY work_date ASC, time_slot ASC")
    List<Schedule> selectSchedulesWithAvailableSlots();

}
