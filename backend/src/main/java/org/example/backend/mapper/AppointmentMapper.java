package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.backend.dto.AppointmentInfoDTO;
import org.example.backend.dto.DailyNewPatientStats;
import org.example.backend.dto.DepartmentAppointmentStats;
import org.example.backend.dto.DoctorAppointmentStats;
import org.example.backend.pojo.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 仅更新 appointment_status、cancel_type、updated_at，不覆盖 payment_status
     */
    @Update("UPDATE appointments " +
            "SET appointment_status = #{appointmentStatus}, " +
            "    updated_at = #{updatedAt} " +
            "WHERE appointment_id = #{appointmentId}")
    int updateStatusOnly(Appointment appointment);

    /*
    获取最大队列数（用于后台调剂时）
     */
    default Integer getMaxQueueNumberByScheduleId(Long scheduleId) {
        QueryWrapper<Appointment> qw = new QueryWrapper<>();
        qw.eq("schedule_id", scheduleId)
                .select("MAX(queue_number) AS maxQueueNumber");

        // 返回一个 Map
        Map<String, Object> result = this.selectMaps(qw).stream().findFirst().orElse(null);

        if (result == null) {
            return 0;
        }

        Object val = result.get("maxQueueNumber");
        return val == null ? 0 : ((Number) val).intValue();
    }

    // 根据 scheduleId 查询挂号（可以按状态过滤）
    @Select("SELECT * FROM appointments WHERE schedule_id = #{scheduleId} AND appointment_status != 'cancelled'")
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
            a.notes AS remarks,

            -- 关联预约信息
            ar.source_appointment_id AS sourceAppointmentId,

            -- 源预约时间
            CONCAT(
                        DATE_FORMAT(src_s.work_date, '%Y年%m月%d日 '),
                        CASE src_s.time_slot
                            WHEN 0 THEN '上午'
                            WHEN 1 THEN '下午'
                            WHEN 2 THEN '晚上'
                            ELSE ''
                        END
                    ) AS sourceAppointmentTime,
            src_app.appointment_status AS sourceStatus,

            -- 新增：源预约医生信息
            src_user.username AS sourceDoctorName,
            src_doc.title AS sourceDoctorTitle

        FROM appointments a
        JOIN patients p ON a.patient_id = p.patient_id
        JOIN users u_patient ON p.user_id = u_patient.user_id
        JOIN schedules s ON a.schedule_id = s.schedule_id
        JOIN doctors doc ON s.doctor_id = doc.doctor_id
        JOIN users u_doctor ON doc.user_id = u_doctor.user_id
        JOIN departments d ON a.dept_id = d.dept_id
        JOIN consultation_rooms r ON a.room_id = r.room_id
        JOIN appointment_types t ON t.appointment_type_id = s.appointment_type_id

        -- 关联旧预约
        LEFT JOIN appointment_relations ar ON ar.target_appointment_id = a.appointment_id
        LEFT JOIN appointments src_app ON ar.source_appointment_id = src_app.appointment_id
        LEFT JOIN schedules src_s ON src_app.schedule_id = src_s.schedule_id

        -- 新增：源预约医生 JOIN
        LEFT JOIN doctors src_doc ON src_s.doctor_id = src_doc.doctor_id
        LEFT JOIN users src_user ON src_doc.user_id = src_user.user_id

        WHERE a.patient_id = #{patientId}
        ORDER BY a.visit_time DESC
        """)
    List<AppointmentInfoDTO> selectAppointmentsByPatientId(@Param("patientId") Long patientId);

    /**
     * 查询过期的未支付订单
     * 
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
     * 
     * @param date 日期
     * @return 当日预约列表
     */
    @Select("SELECT a.* FROM appointments a " +
            "INNER JOIN schedules s ON a.schedule_id = s.schedule_id " +
            "WHERE a.payment_status = 'paid' " +
            "AND a.appointment_status = 'booked' " +
            "AND DATE(s.work_date) = #{date}")
    List<Appointment> selectTodayPaidAppointments(@Param("date") LocalDate date);

    /**
     * 统计医生挂号数量
     */
    List<DoctorAppointmentStats> selectDoctorAppointmentStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计科室挂号数量
     */
    List<DepartmentAppointmentStats> selectDepartmentAppointmentStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计每日新增患者
     */
    List<DailyNewPatientStats> selectDailyNewPatientStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 患者端：查询患者所有已完成预约（连表查询，返回DTO）
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
            a.notes AS remarks,

            -- 关联预约信息
            ar.source_appointment_id AS sourceAppointmentId,

            -- 源预约时间
            CONCAT(
                        DATE_FORMAT(src_s.work_date, '%Y年%m月%d日 '),
                        CASE src_s.time_slot
                            WHEN 0 THEN '上午'
                            WHEN 1 THEN '下午'
                            WHEN 2 THEN '晚上'
                            ELSE ''
                        END
                    ) AS sourceAppointmentTime,
            src_app.appointment_status AS sourceStatus,

            -- 新增：源预约医生信息
            src_user.username AS sourceDoctorName,
            src_doc.title AS sourceDoctorTitle

        FROM appointments a
        JOIN patients p ON a.patient_id = p.patient_id
        JOIN users u_patient ON p.user_id = u_patient.user_id
        JOIN schedules s ON a.schedule_id = s.schedule_id
        JOIN doctors doc ON s.doctor_id = doc.doctor_id
        JOIN users u_doctor ON doc.user_id = u_doctor.user_id
        JOIN departments d ON a.dept_id = d.dept_id
        JOIN consultation_rooms r ON a.room_id = r.room_id
        JOIN appointment_types t ON t.appointment_type_id = s.appointment_type_id

        -- 关联旧预约
        LEFT JOIN appointment_relations ar ON ar.target_appointment_id = a.appointment_id
        LEFT JOIN appointments src_app ON ar.source_appointment_id = src_app.appointment_id
        LEFT JOIN schedules src_s ON src_app.schedule_id = src_s.schedule_id

        -- 新增：源预约医生 JOIN
        LEFT JOIN doctors src_doc ON src_s.doctor_id = src_doc.doctor_id
        LEFT JOIN users src_user ON src_doc.user_id = src_user.user_id

        WHERE a.patient_id = #{patientId}
            AND a.appointment_status = 'completed'
        ORDER BY a.visit_time DESC
        """)
    List<AppointmentInfoDTO> selectCompletedAppointmentsByPatientId(@Param("patientId") Long patientId);


    /**
     * 计算指定排班下已支付预约的数量
     * 用于生成新的排队号（只统计已支付的预约）
     *
     * @param scheduleId 排班ID
     * @return 已支付的预约数量
     */
    @Select("SELECT COUNT(*) FROM appointments " +
            "WHERE schedule_id = #{scheduleId} " +
            "AND payment_status = 'paid' " +
            "AND appointment_status NOT IN ('cancelled', 'no_show')")
    int countPaidAppointments(@Param("scheduleId") Integer scheduleId);

    /**
     * 获取指定排班下的最大排队号
     * 用于生成新排队号时的参考
     *
     * @param scheduleId 排班ID
     * @return 最大排队号，如果没有记录则返回0
     */
    @Select("SELECT COALESCE(MAX(queue_number), 0) FROM appointments " +
            "WHERE schedule_id = #{scheduleId} " +
            "AND payment_status = 'paid' " +
            "AND appointment_status NOT IN ('cancelled', 'no_show')")
    int getMaxQueueNumber(@Param("scheduleId") Integer scheduleId);

    /**
     * 重新计算并更新指定排班下所有已支付预约的排队号
     * 按照 booking_time 升序重新分配排队号（1, 2, 3...）
     *
     * @param scheduleId 排班ID
     * @return 受影响的行数
     */
    @Update("UPDATE appointments a " +
            "JOIN ( " +
            "    SELECT appointment_id, " +
            "           ROW_NUMBER() OVER (ORDER BY booking_time ASC) as new_queue " +
            "    FROM appointments " +
            "    WHERE schedule_id = #{scheduleId} " +
            "    AND payment_status = 'paid' " +
            "    AND appointment_status NOT IN ('cancelled', 'no_show') " +
            ") b ON a.appointment_id = b.appointment_id " +
            "SET a.queue_number = b.new_queue, " +
            "    a.updated_at = NOW()")
    int recalculateQueueNumbers(@Param("scheduleId") Integer scheduleId);

    /**
     * 批量更新排队号（优化版本，适用于大批量更新）
     *
     * @param queueMap
     */
    @Update("<script>" +
            "UPDATE appointments " +
            "SET queue_number = CASE appointment_id " +
            "<foreach collection='queueMap' index='appointmentId' item='queueNum' separator=' '>" +
            "WHEN #{appointmentId} THEN #{queueNum} " +
            "</foreach>" +
            "END, " +
            "updated_at = NOW() " +
            "WHERE appointment_id IN " +
            "<foreach collection='queueMap.keySet()' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateQueueNumbers(@Param("queueMap") Map<Long, Integer> queueMap);
}
