package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.AppointmentRelations;
@Mapper
public interface AppointmentRelationsMapper extends BaseMapper<AppointmentRelations> {

    /**
     * 递归查询改约链的深度（改约次数）
     * 从当前预约往前追溯，统计一共改约了多少次
     *
     * @param appointmentId 当前预约ID
     * @return 改约次数（0表示未改约，1表示改约1次，以此类推）
     */
    @Select("WITH RECURSIVE reschedule_chain AS ( " +
            "  SELECT " +
            "    source_appointment_id, " +
            "    target_appointment_id, " +
            "    1 AS depth " +
            "  FROM appointment_relations " +
            "  WHERE target_appointment_id = #{appointmentId} " +
            "    AND relation_type IN ('MANUAL_RESCHEDULE', 'AUTO_REASSIGN') " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT " +
            "    ar.source_appointment_id, " +
            "    ar.target_appointment_id, " +
            "    rc.depth + 1 " +
            "  FROM appointment_relations ar " +
            "  INNER JOIN reschedule_chain rc " +
            "    ON ar.target_appointment_id = rc.source_appointment_id " +
            "  WHERE ar.relation_type IN ('MANUAL_RESCHEDULE', 'AUTO_REASSIGN') " +
            "    AND rc.depth < 10  " +
            ") " +
            "SELECT COALESCE(MAX(depth), 0) AS reschedule_count " +
            "FROM reschedule_chain")
    Integer countRescheduleDepth(@Param("appointmentId") Long appointmentId);

}
