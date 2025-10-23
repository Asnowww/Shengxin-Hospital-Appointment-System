package org.example.backend.controller;


import jakarta.annotation.Resource;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.pojo.Doctor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Resource
    private DoctorMapper doctorMapper;

    // 查询所有医生
    @GetMapping("/list")
    public List<Doctor> getAllDoctors() {
        return doctorMapper.selectList(null);
    }

    // 根据 ID 查询医生详情
    @GetMapping("/{id}")
    public Doctor getDoctorById(@PathVariable Long id) {
        return doctorMapper.selectById(id);
    }
}

