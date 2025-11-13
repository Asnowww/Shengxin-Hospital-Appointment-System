package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.dto.*;
import org.example.backend.service.AppointmentService;
import org.example.backend.service.DoctorService;
import org.example.backend.service.PatientService;
import org.example.backend.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class StatisticsController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private DoctorService doctorService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private PatientService patientService;

    /**
     * 获取医生挂号数量统计
     */
    @GetMapping("/doctor-appointments")
    public Result<List<DoctorAppointmentStats>> getDoctorAppointmentStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<DoctorAppointmentStats> stats = appointmentService.getDoctorAppointmentStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取医生挂号统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取科室挂号数量统计
     */
    @GetMapping("/department-appointments")
    public Result<List<DepartmentAppointmentStats>> getDepartmentAppointmentStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<DepartmentAppointmentStats> stats = appointmentService.getDepartmentAppointmentStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取科室挂号统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取排班利用率统计
     */
    @GetMapping("/schedule-utilization")
    public Result<List<ScheduleUtilizationStats>> getScheduleUtilizationStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<ScheduleUtilizationStats> stats = scheduleService.getScheduleUtilizationStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取排班利用率统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取医生工作量统计
     */
    @GetMapping("/doctor-workload")
    public Result<List<DoctorWorkloadStats>> getDoctorWorkloadStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<DoctorWorkloadStats> stats = doctorService.getDoctorWorkloadStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取医生工作量统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取每日新增患者统计
     */
    @GetMapping("/daily-new-patients")
    public Result<List<DailyNewPatientStats>> getDailyNewPatientStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<DailyNewPatientStats> stats = patientService.getDailyNewPatientStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取每日新增患者统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取排班覆盖率统计 - 按科室返回列表
     */
    @GetMapping("/schedule-coverage")
    public Result<List<ScheduleCoverageStats>> getScheduleCoverageStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<ScheduleCoverageStats> stats = scheduleService.getDepartmentScheduleCoverageStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取排班覆盖率统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取医生出诊率统计
     */
    @GetMapping("/doctor-attendance")
    public Result<List<DoctorAttendanceStats>> getDoctorAttendanceStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            List<DoctorAttendanceStats> stats = doctorService.getDoctorAttendanceStats(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取医生出诊率统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取综合统计数据（仪表盘）
     */
    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboardStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            DashboardStats dashboard = new DashboardStats();
            dashboard.setDoctorAppointments(appointmentService.getDoctorAppointmentStats(startDate, endDate));
            dashboard.setDepartmentAppointments(appointmentService.getDepartmentAppointmentStats(startDate, endDate));
            dashboard.setScheduleUtilization(scheduleService.getScheduleUtilizationStats(startDate, endDate));
            dashboard.setDoctorWorkload(doctorService.getDoctorWorkloadStats(startDate, endDate));
            dashboard.setDailyNewPatients(patientService.getDailyNewPatientStats(startDate, endDate));
            dashboard.setScheduleCoverage(scheduleService.getDepartmentScheduleCoverageStats(startDate, endDate));
            dashboard.setDoctorAttendance(doctorService.getDoctorAttendanceStats(startDate, endDate));
            return Result.success(dashboard);
        } catch (Exception e) {
            return Result.error("获取综合统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定医生的详细统计
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<DoctorDetailStats> getDoctorDetailStats(
            @PathVariable Long doctorId,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            // 获取该医生的各项统计数据
            List<DoctorAppointmentStats> appointmentStats = appointmentService.getDoctorAppointmentStats(startDate, endDate);
            List<ScheduleUtilizationStats> utilizationStats = scheduleService.getScheduleUtilizationStats(startDate, endDate);
            List<DoctorWorkloadStats> workloadStats = doctorService.getDoctorWorkloadStats(startDate, endDate);
            List<DoctorAttendanceStats> attendanceStats = doctorService.getDoctorAttendanceStats(startDate, endDate);

            // 筛选该医生的数据
            DoctorDetailStats detailStats = new DoctorDetailStats();
            appointmentStats.stream()
                    .filter(s -> s.getDoctorId().equals(doctorId))
                    .findFirst()
                    .ifPresent(detailStats::setAppointmentStats);

            utilizationStats.stream()
                    .filter(s -> s.getDoctorId().equals(doctorId))
                    .findFirst()
                    .ifPresent(detailStats::setUtilizationStats);

            workloadStats.stream()
                    .filter(s -> s.getDoctorId().equals(doctorId))
                    .findFirst()
                    .ifPresent(detailStats::setWorkloadStats);

            attendanceStats.stream()
                    .filter(s -> s.getDoctorId().equals(doctorId))
                    .findFirst()
                    .ifPresent(detailStats::setAttendanceStats);

            return Result.success(detailStats);
        } catch (Exception e) {
            return Result.error("获取医生详细统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定科室的详细统计
     */
    @GetMapping("/department/{deptId}")
    public Result<DepartmentDetailStats> getDepartmentDetailStats(
            @PathVariable Integer deptId,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            // 获取该科室的统计数据
            List<DepartmentAppointmentStats> appointmentStats = appointmentService.getDepartmentAppointmentStats(startDate, endDate);
            List<DoctorAppointmentStats> doctorStats = appointmentService.getDoctorAppointmentStats(startDate, endDate);

            DepartmentDetailStats detailStats = new DepartmentDetailStats();

            // 科室整体统计
            appointmentStats.stream()
                    .filter(s -> s.getDeptId().equals(deptId))
                    .findFirst()
                    .ifPresent(detailStats::setDepartmentStats);

            // 该科室下的医生统计
            List<DoctorAppointmentStats> deptDoctors = doctorStats.stream()
                    .filter(s -> s.getDeptId().equals(deptId))
                    .toList();
            detailStats.setDoctorStats(deptDoctors);

            return Result.success(detailStats);
        } catch (Exception e) {
            return Result.error("获取科室详细统计失败: " + e.getMessage());
        }
    }

    // ========== DTO 类定义 ==========

    /**
     * 综合仪表盘统计数据
     */
    @lombok.Data
    public static class DashboardStats {
        private List<DoctorAppointmentStats> doctorAppointments;
        private List<DepartmentAppointmentStats> departmentAppointments;
        private List<ScheduleUtilizationStats> scheduleUtilization;
        private List<DoctorWorkloadStats> doctorWorkload;
        private List<DailyNewPatientStats> dailyNewPatients;
        private List<ScheduleCoverageStats> scheduleCoverage;
        private List<DoctorAttendanceStats> doctorAttendance;
    }

    /**
     * 医生详细统计
     */
    @lombok.Data
    public static class DoctorDetailStats {
        private DoctorAppointmentStats appointmentStats;
        private ScheduleUtilizationStats utilizationStats;
        private DoctorWorkloadStats workloadStats;
        private DoctorAttendanceStats attendanceStats;
    }

    /**
     * 科室详细统计
     */
    @lombok.Data
    public static class DepartmentDetailStats {
        private DepartmentAppointmentStats departmentStats;
        private List<DoctorAppointmentStats> doctorStats;
    }
}

