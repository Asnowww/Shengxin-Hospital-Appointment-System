package org.example.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class DoctorSchedulePatientsVO {
    /** 排班ID */
    private Integer scheduleId;

    /** 工作日期 */
    private LocalDate workDate;

    /** 时间段 (0=上午, 1=下午, 2=晚上) */
    private Integer timeSlot;

    /** 时间段名称 */
    private String timeSlotName;

    /** 科室名称 */
    private String deptName;

    /** 诊室名称 */
    private String roomName;

    /** 该排班下的患者列表 */
    private List<SchedulePatientVO> patients;
}