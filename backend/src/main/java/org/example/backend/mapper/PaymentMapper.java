package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.Payments;

@Mapper
public interface PaymentMapper extends BaseMapper<Payments> {

    /**
     * 根据预约ID查询支付记录
     */
    @Select("SELECT * FROM payments WHERE appointment_id = #{appointmentId} LIMIT 1")
    Payments selectByAppointmentId(Long appointmentId);
}
