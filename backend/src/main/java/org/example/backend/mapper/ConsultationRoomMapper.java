package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.pojo.ConsultationRoom;

@Mapper
public interface ConsultationRoomMapper extends BaseMapper<ConsultationRoom> {

    /**
     * 根据科室ID删除诊室
     */
    @Delete("DELETE FROM consultation_rooms WHERE dept_id = #{deptId}")
    int deleteByDeptId(@Param("deptId") Integer deptId);
}