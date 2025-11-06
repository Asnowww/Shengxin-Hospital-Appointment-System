package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.DoctorVO;
import org.example.backend.pojo.Doctor;

import java.util.List;

public interface DoctorService extends IService<Doctor> {
    List<DoctorVO> getAllDoctorsWithNameAndDept() ;

    void addDoctor(DoctorVO doctorVO);
    void updateDoctorInfo(DoctorVO doctorVO);
}
