package org.example.backend.service;

import org.example.backend.dto.*;
import org.example.backend.pojo.Appointment;
import org.example.backend.pojo.AppointmentType;
import org.example.backend.pojo.Patient;
import org.springframework.transaction.annotation.Transactional;

import org.example.backend.dto.AppointmentInfoDTO;

import java.math.BigDecimal;
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
     * 验证预约是否可以修改
     * @param appointmentId 预约ID
     * @param patientId 患者ID
     * @return 是否可修改
     */
    boolean canUpdateAppointment(Long appointmentId, Long patientId);

    List<DoctorAppointmentStats> getDoctorAppointmentStats(LocalDate startDate, LocalDate endDate);

    List<DepartmentAppointmentStats> getDepartmentAppointmentStats(LocalDate startDate, LocalDate endDate);


    /**
     * 返回给前端费用相关数据
     */
    List<AppointmentType> getAllAppointmentTypes();

    /**
     * 通过返回的appointmentId，计算对应号别和患者的报销比例
     * @param appointmentId 挂号id
     * @return 实际支付费用
     */
    Result<Object> calculateFee(Long appointmentId);

    BigDecimal computeFinalFee(AppointmentType type, Patient patient);
}