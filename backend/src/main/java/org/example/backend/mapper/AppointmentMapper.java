package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.Appointment;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
