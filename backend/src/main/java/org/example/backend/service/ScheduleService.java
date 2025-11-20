package org.example.backend.service;

import org.example.backend.dto.*;
import org.example.backend.pojo.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    /**
     * 批量创建排班（支持按周模板）
     */
    void batchCreateSchedules(ScheduleCreateParam param);

    /**
     * 创建排班单例排班
     */
    void createSchedules(ScheduleCreateParam param);

    /**
     * 修改排班
     */
    void updateSchedule(ScheduleUpdateParam param);

    /**
     * 停诊（取消排班）
     */
    void cancelSchedule(Integer scheduleId, String reason, Long operatorId);

    /**
     * 查询排班详情
     */
    ScheduleDetailVO getScheduleDetail(Integer scheduleId);

    /**
     * 查询医生的排班列表
     */
    List<ScheduleDetailVO> getDoctorSchedules(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询科室的排班列表
     */
    List<ScheduleDetailVO> getDepartmentSchedules(Integer deptId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询所有排班（管理员用，支持多条件筛选）
     */
    List<ScheduleDetailVO> getAllSchedules(Integer deptId, Long doctorId, LocalDate startDate, LocalDate endDate, String status);

    /**
     * 临时加号
     */
    void addExtraSlots(AddExtraSlotsParam param);

    /**
     * 检查医生在指定时间段是否有排班冲突
     */
    boolean checkDoctorScheduleConflict(Long doctorId, LocalDate workDate, Integer timeSlot);

    /**
     * 查询受影响的挂号记录
     */
    List<Long> getAffectedAppointments(Integer scheduleId);

    /**
     * 推荐替代排班（同科室其他医生）
     */
    List<AlternativeScheduleVO> recommendAlternativeSchedules(Integer scheduleId, int limit);

    List<ScheduleUtilizationStats> getScheduleUtilizationStats(LocalDate startDate, LocalDate endDate);

    List<ScheduleCoverageStats> getDepartmentScheduleCoverageStats(LocalDate startDate, LocalDate endDate);

    List<DoctorSchedulePatientsVO> getDoctorSchedulePatientsGrouped(Long doctorId, LocalDate date);

    List<SchedulePatientVO> getSchedulePatients(Integer scheduleId);

}
