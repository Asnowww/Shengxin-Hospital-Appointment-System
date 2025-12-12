package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.AppointmentRelations;
@Mapper
public interface AppointmentRelationsMapper extends BaseMapper<AppointmentRelations> {

}
