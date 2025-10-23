package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.pojo.Appointment;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 查询医生的所有预约
     * @param doctorId 医生ID
     * @return 预约列表
     */
    List<Appointment> selectAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 查询医生当天的预约
     * @param doctorId 医生ID
     * @param date 日期（LocalDate）
     * @return 当天预约列表
     */
    List<Appointment> selectAppointmentsByDoctorIdAndDate(@Param("doctorId") Long doctorId,
                                                          @Param("date") LocalDate date);
}
