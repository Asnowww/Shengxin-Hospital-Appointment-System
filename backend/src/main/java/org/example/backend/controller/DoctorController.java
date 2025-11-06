package org.example.backend.controller;


import jakarta.annotation.Resource;
import org.example.backend.dto.DoctorVO;
import org.example.backend.dto.Result;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.pojo.Doctor;
import org.example.backend.service.DoctorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private DoctorService doctorService;

    // 查询所有医生
    @GetMapping("/list")
    public Result<List<DoctorVO>> getAllDoctors() {
        List<DoctorVO> doctors = doctorService.getAllDoctorsWithNameAndDept();
        return Result.success(doctors);
    }


    // 根据 ID 查询医生详情
    @GetMapping("/{id}")
    public Doctor getDoctorById(@PathVariable Long id) {
        return doctorMapper.selectById(id);
    }
}

