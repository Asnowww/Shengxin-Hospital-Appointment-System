package org.example.backend.service;

import org.example.backend.dto.*;
import org.example.backend.pojo.DoctorLeave;
import org.example.backend.pojo.ScheduleException;

import java.util.List;

public interface DoctorLeaveService {

    /**
     * 医生申请请假
     */
    void applyLeave(LeaveApplyParam param);

    /**
     * 医生申请调班
     */
    void applyScheduleAdjust(ScheduleAdjustParam param);

    /**
     * 管理员审批请假
     */
    void reviewLeave(LeaveReviewParam param);

    /**
     * 管理员审批调班
     */
    void reviewScheduleAdjust(Long exceptionId, String action, Long reviewedBy);

    /**
     * 查询请假申请列表
     */
    List<DoctorLeaveVO> getLeaveApplications(String status, Long doctorId);

    /**
     * 查询调班申请列表
     */
    List<ScheduleException> getAdjustApplications(String status, Long doctorId);

    /**
     * 查询医生的请假历史
     */
    List<DoctorLeave> getDoctorLeaveHistory(Long doctorId);
}
