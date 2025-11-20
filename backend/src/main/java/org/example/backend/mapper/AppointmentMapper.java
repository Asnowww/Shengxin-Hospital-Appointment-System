package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.dto.AppointmentInfoDTO;
import org.example.backend.dto.DailyNewPatientStats;
import org.example.backend.dto.DepartmentAppointmentStats;
import org.example.backend.dto.DoctorAppointmentStats;
import org.example.backend.pojo.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    // 根据 scheduleId 查询挂号（可以按状态过滤）
    List<Appointment> selectByScheduleId(@Param("scheduleId") Integer scheduleId);

    /**
     * 查询医生的所有预约
     */
    List<Appointment> selectAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 查询医生当天的预约
     */
    List<Appointment> selectAppointmentsByDoctorIdAndDate(@Param("doctorId") Long doctorId,
                                                          @Param("date") LocalDate date);

    /**
     * 患者端：查询患者所有预约（连表查询，返回DTO）
     */
    @Select("""
        SELECT
            a.appointment_id,
            a.patient_id,
            u_patient.username AS patient_name,
            u_doctor.username AS doctor_name,
            doc.title AS doctor_title,
            doc.bio AS doctor_info,
            d.dept_name,
            r.building,
            r.room_name,
            t.type_name,
            CONCAT(
                        DATE_FORMAT(s.work_date, '%Y年%m月%d日 '),
                        CASE s.time_slot
                            WHEN 0 THEN '上午'
                            WHEN 1 THEN '下午'
                            WHEN 2 THEN '晚上'
                            ELSE ''
                        END
                    ) AS appointmentTime,
            DATE_FORMAT(a.booking_time, '%Y-%m-%d %H:%i:%s') AS bookingTime,
            a.appointment_status AS status,
            a.fee_original,
            a.fee_final,
            a.notes AS remarks
        FROM appointments a
        JOIN patients p ON a.patient_id = p.patient_id
        JOIN users u_patient ON p.user_id = u_patient.user_id
        JOIN schedules s ON a.schedule_id = s.schedule_id
        JOIN doctors doc ON s.doctor_id = doc.doctor_id
        JOIN users u_doctor ON doc.user_id = u_doctor.user_id
        JOIN departments d ON a.dept_id = d.dept_id
        JOIN consultation_rooms r ON a.room_id = r.room_id
        JOIN appointment_types t ON t.appointment_type_id = s.appointment_type_id
        WHERE a.patient_id = #{patientId}
        ORDER BY a.visit_time DESC
        """)
    List<AppointmentInfoDTO> selectAppointmentsByPatientId(@Param("patientId") Long patientId);


    /**
     * 查询过期的未支付订单
     * @param currentTime 当前时间
     * @return 过期订单列表
     */
    @Select("SELECT * FROM appointments " +
            "WHERE appointment_status = 'pending' " +
            "AND payment_status = 'unpaid' " +
            "AND expire_time <= #{currentTime}")
    List<Appointment> selectExpiredUnpaidAppointments(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询当日已支付待就诊的预约
     * @param date 日期
     * @return 当日预约列表
     */
    @Select("SELECT a.* FROM appointment a " +
            "INNER JOIN schedules s ON a.schedule_id = s.schedule_id " +
            "WHERE a.payment_status = 'paid' " +
            "AND a.appointment_status = 'booked' " +
            "AND DATE(s.schedule_date) = #{date}")
    List<Appointment> selectTodayPaidAppointments(@Param("date") LocalDate date);

    /**
     * 统计医生挂号数量
     */
    @Select("<script>" +
            "SELECT " +
            "    d.doctor_id, " +
            "    u.username AS doctor_name, " +
            "    d.dept_id, " +
            "    dept.dept_name, " +
            "    COUNT(a.appointment_id) AS appointment_count, " +
            "    SUM(CASE WHEN a.appointment_status = 'completed' THEN 1 ELSE 0 END) AS completed_count, " +
            "    SUM(CASE WHEN a.appointment_status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled_count, " +
            "    SUM(CASE WHEN a.payment_status = 'paid' THEN a.fee_final ELSE 0 END) AS total_revenue " +
            "FROM appointments a " +
            "INNER JOIN schedules s ON a.schedule_id = s.schedule_id " +
            "INNER JOIN doctors d ON s.doctor_id = d.doctor_id " +
            "INNER JOIN users u ON d.user_id = u.user_id " +
            "INNER JOIN departments dept ON d.dept_id = dept.dept_id " +
            "WHERE s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY d.doctor_id, u.username, d.dept_id, dept.dept_name " +
            "ORDER BY appointment_count DESC" +
            "</script>")
    List<DoctorAppointmentStats> selectDoctorAppointmentStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计科室挂号数量
     */
    @Select("<script>" +
            "SELECT " +
            "    dept.dept_id, " +
            "    dept.dept_name, " +
            "    COUNT(a.appointment_id) AS appointment_count, " +
            "    SUM(CASE WHEN a.appointment_status = 'completed' THEN 1 ELSE 0 END) AS completed_count, " +
            "    SUM(CASE WHEN a.appointment_status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled_count, " +
            "    SUM(CASE WHEN a.payment_status = 'paid' THEN a.fee_final ELSE 0 END) AS total_revenue " +
            "FROM appointments a " +
            "INNER JOIN schedules s ON a.schedule_id = s.schedule_id " +
            "INNER JOIN departments dept ON a.dept_id = dept.dept_id " +
            "WHERE s.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY dept.dept_id, dept.dept_name " +
            "ORDER BY appointment_count DESC" +
            "</script>")
    List<DepartmentAppointmentStats> selectDepartmentAppointmentStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计每日新增患者
     */
    @Select("<script>" +
            "SELECT " +
            "    DATE(a.booking_time) AS date, " +
            "    COUNT(DISTINCT a.patient_id) AS new_patient_count " +
            "FROM appointments a " +
            "WHERE DATE(a.booking_time) BETWEEN #{startDate} AND #{endDate} " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM appointments a2 " +
            "    WHERE a2.patient_id = a.patient_id " +
            "    AND DATE(a2.booking_time) &lt; DATE(#{startDate})" +  // 修复这里：< 改为 &lt;
            ") " +
            "GROUP BY DATE(a.booking_time) " +
            "ORDER BY date" +
            "</script>")
    List<DailyNewPatientStats> selectDailyNewPatientStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
