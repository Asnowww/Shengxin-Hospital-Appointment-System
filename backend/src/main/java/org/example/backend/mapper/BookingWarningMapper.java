package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.BookingWarning;

import java.time.LocalDateTime;

@Mapper
public interface BookingWarningMapper extends BaseMapper<BookingWarning> {

    /**
     * 统计指定时间之后患者的警告次数
     * @param patientId 患者ID
     * @param sinceTime 开始时间（统计此时间之后的警告）
     * @return 警告次数
     */
    @Select("SELECT COUNT(*) FROM booking_warnings " +
            "WHERE patient_id = #{patientId} " +
            "AND warning_time >= #{sinceTime}")
    Long countWarningsSince(@Param("patientId") Long patientId,
                            @Param("sinceTime") LocalDateTime sinceTime);
}