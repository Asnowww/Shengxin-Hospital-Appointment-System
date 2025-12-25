package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.BannedUser;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BannedUserMapper extends BaseMapper<BannedUser> {

    @Select("SELECT * FROM banned_users " +
            "WHERE user_id = #{userId} " +
            "AND is_active = TRUE " +
            "AND ban_end_time > NOW() " +
            "ORDER BY ban_end_time DESC " +
            "LIMIT 1")
    BannedUser selectActiveByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM banned_users " +
            "WHERE is_active = TRUE " +
            "AND ban_end_time <= #{currentTime}")
    List<BannedUser> selectExpiredBans(@Param("currentTime") LocalDateTime currentTime);


    //爽约统计
    @Select("SELECT COUNT(*) FROM appointments " +
            "WHERE patient_id = #{patientId} " +
            "AND appointment_status = 'no_show' " +
            "AND visit_time >= #{sinceTime}")
    Long countNoShowSince(@Param("patientId") Long patientId,
                          @Param("sinceTime") LocalDateTime sinceTime);


    //取消预约统计（不计入系统强制取消的）
    @Select("SELECT COUNT(*) FROM appointments " +
            "WHERE patient_id = #{patientId} " +
            "AND appointment_status = 'cancelled' " +
            "AND (notes IS NULL OR notes != '系统强制取消') " +
            "AND booking_time >= #{sinceTime}")
    Long countCancelSince(@Param("patientId") Long patientId,
                          @Param("sinceTime") LocalDateTime sinceTime);
}
