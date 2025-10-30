package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.Waitlist;
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
}
