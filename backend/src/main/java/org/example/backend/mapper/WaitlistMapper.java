package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.Waitlist;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WaitlistMapper extends BaseMapper<Waitlist> {

    @Select("SELECT * FROM waitlist " +
            "WHERE schedule_id = #{scheduleId} AND status = 'waiting' " +
            "ORDER BY priority DESC, requested_at ASC")
    List<Waitlist> selectWaitingListByScheduleId(Integer scheduleId);

    @Select("SELECT * FROM waitlist " +
            "WHERE patient_id = #{patientId} " +
            "ORDER BY requested_at DESC")
    List<Waitlist> selectByPatientId(Long patientId);


    /**
     * 查询过期的未支付订单
     *
     * @return 过期订单列表
     */
    @Select("SELECT w.* FROM waitlist w " +
            "JOIN schedules s ON w.schedule_id = s.schedule_id " +
            "WHERE w.status = 'waiting' " +
            "AND (s.work_date < CURDATE() " +
            "     OR (s.work_date = CURDATE() AND " +
            "         ((s.time_slot = 0 AND CURTIME() >= '12:00:00') " +
            "          OR (s.time_slot = 1 AND CURTIME() >= '18:00:00') " +
            "          OR (s.time_slot = 2 AND CURTIME() >= '21:00:00')))) " +
            "ORDER BY w.wait_id ASC")
    List<Waitlist> selectExpiredWaitlistRecords();

    /**
     * 查询患者在同一时段的其他候补记录
     * @param patientId 患者ID
     * @param workDate 排班日期
     * @param timeSlot 时间段
     * @param excludeWaitId 要排除的候补ID（当前转正的候补）
     * @return 同时段的其他候补列表
     */
    @Select("SELECT w.* FROM waitlist w " +
            "JOIN schedules s ON w.schedule_id = s.schedule_id " +
            "WHERE w.patient_id = #{patientId} " +
            "AND w.wait_id != #{excludeWaitId} " +
            "AND w.status IN ('waiting', 'notified') " +
            "AND s.work_date = #{workDate} " +
            "AND s.time_slot = #{timeSlot} " +
            "ORDER BY w.wait_id ASC")
    List<Waitlist> selectSameTimeSlotWaitlists(@Param("patientId") Long patientId,
                                               @Param("workDate") LocalDate workDate,
                                               @Param("timeSlot") Integer timeSlot,
                                               @Param("excludeWaitId") Long excludeWaitId);
}
