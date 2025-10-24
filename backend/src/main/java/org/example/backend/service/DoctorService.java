package org.example.backend.service;

import org.example.backend.dto.DoctorVO;

import java.util.List;

public interface DoctorService {
    public List<DoctorVO> getAllDoctorsWithNameAndDept() ;
}
