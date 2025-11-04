package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.Refunds;

@Mapper
public interface RefundMapper extends BaseMapper<Refunds> {

    /**
     * 根据预约ID查询退款记录
     */
    @Select("SELECT * FROM refunds WHERE appointment_id = #{appointmentId} LIMIT 1")
    Refunds selectByAppointmentId(Long appointmentId);

    /**
     * 根据支付ID查询退款记录
     */
    @Select("SELECT * FROM refunds WHERE payment_id = #{paymentId} LIMIT 1")
    Refunds selectByPaymentId(Long paymentId);
}
