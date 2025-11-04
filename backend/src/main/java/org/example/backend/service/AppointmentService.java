package org.example.backend.service;

import org.example.backend.dto.AppointmentCreateParam;
import org.example.backend.dto.AppointmentUpdateParam;
import org.example.backend.pojo.Appointment;
import org.springframework.transaction.annotation.Transactional;

import org.example.backend.dto.AppointmentInfoDTO;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // === 病人端 ===

    /**
     * 创建预约（患者端挂号）
     * @param param 预约参数
     * @return 创建的预约对象
     */
    Appointment createAppointmentByPatient(AppointmentCreateParam param);


    /**
     * 根据患者ID查询所有预约
     */
    List<AppointmentInfoDTO> getAppointmentsByPatientId(Long patientId);

    /**
     * 根据患者ID和日期查询预约
     */
    List<Appointment> getAppointmentsByPatientIdAndDate(Long patientId, LocalDate date);

    /**
     * 根据预约ID查询单个预约详情
     */
    Appointment getById(Long appointmentId);

    /**
     * 取消预约
     */
    boolean cancelAppointment(Long appointmentId, Long patientId);

    // === 医生端 ===
    /**
     * 根据医生ID查询所有预约
     */
    List<Appointment> getAppointmentsByDoctorId(Long doctorId);

    /**
     * 根据医生ID查询当天预约
     */
    List<Appointment> getTodayAppointmentsByDoctorId(Long doctorId);

    // === 管理员端 ===
    /**
     * 查询所有预约
     */
    List<Appointment> getAllAppointments();

    /**
     * 设置号别费用
     */
    boolean updateAppointmentFee(Integer id, Double fee);

    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long appointmentId, String status);

    /**
     * 修改预约（改期、改时间段）
     * @param param 修改参数
     * @return 是否修改成功
     */
    @Transactional
    boolean updateAppointment(AppointmentUpdateParam param);

    /**
     *
     * 支付挂号订单
     * @param appointmentId
     * @param patientId
     * @param method
     * @return
     */
    boolean payAppointment(Long appointmentId, Long patientId, String method);

    /**
     * 处理预约退款
     * @param appointmentId 预约ID
     * @param patientId 患者ID（可选，用于权限验证）
     * @param reason 退款原因
     * @return 是否退款成功
     */
    boolean refundAppointment(Long appointmentId, Long patientId, String reason);



    /**
     * 验证预约是否可以修改
     * @param appointmentId 预约ID
     * @param patientId 患者ID
     * @return 是否可修改
     */
    boolean canUpdateAppointment(Long appointmentId, Long patientId);
}