package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.Waitlist;

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
}
