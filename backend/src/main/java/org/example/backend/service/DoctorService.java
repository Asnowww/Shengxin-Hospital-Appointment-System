package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.DoctorAttendanceStats;
import org.example.backend.dto.DoctorVO;
import org.example.backend.dto.DoctorWorkloadStats;
import org.example.backend.pojo.Doctor;

import java.time.LocalDate;
import java.util.List;

public interface DoctorService extends IService<Doctor> {
    List<DoctorVO> getAllDoctorsWithNameAndDept() ;
    List<DoctorVO> getDoctorVOByDeptId(Integer deptId);

    DoctorVO getDoctorById(Long doctorId);
    void addDoctor(DoctorVO doctorVO);
    void updateDoctorInfo(DoctorVO doctorVO);

    List<DoctorWorkloadStats> getDoctorWorkloadStats(LocalDate startDate, LocalDate endDate);

    List<DoctorAttendanceStats> getDoctorAttendanceStats(LocalDate startDate, LocalDate endDate);
}
