package org.example.backend.controller;

import org.example.backend.dto.DoctorAccountDTO;
import org.example.backend.dto.DoctorQueryDTO;
import org.example.backend.pojo.DoctorBioUpdateRequest;
import org.example.backend.service.DoctorAccountService;
import org.example.backend.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理员 - 医生账号管理控制器
 */
@RestController
@RequestMapping("/api/admin/doctors")
public class AdminDoctorAccountController {

    @Autowired
    private DoctorAccountService doctorAccountService;

    /**
     * 查询医生账号列表(支持筛选)
     */
    @GetMapping("/list")
    public Result<List<DoctorAccountDTO>> getDoctorList(DoctorQueryDTO queryDTO) {
        List<DoctorAccountDTO> doctors = doctorAccountService.getDoctorList(queryDTO);
        return Result.success(doctors);
    }

    /**
     * 新增医生账号
     */
    @PostMapping("/add")
    public Result<String> addDoctor(@RequestBody DoctorAccountDTO doctorDTO) {
        doctorAccountService.addDoctor(doctorDTO);
        return Result.success("医生账号创建成功");
    }

    /**
     * 修改医生信息
     */
    @PutMapping("/update/{doctorId}")
    public Result<String> updateDoctor(
            @PathVariable Long doctorId,
            @RequestBody DoctorAccountDTO doctorDTO) {
        doctorDTO.setDoctorId(doctorId);
        doctorAccountService.updateDoctor(doctorDTO);
        return Result.success("医生信息更新成功");
    }

    /**
     * 禁用/启用医生账号
     */
    @PutMapping("/status/{doctorId}")
    public Result<String> updateDoctorStatus(
            @PathVariable Long doctorId,
            @RequestParam String status) {
        doctorAccountService.updateDoctorStatus(doctorId, status);
        String msg = "verified".equals(status) ? "启用成功" : "禁用成功";
        return Result.success(msg);
    }

    /**
     * 重置医生密码
     */
    @PutMapping("/reset-password/{doctorId}")
    public Result<String> resetPassword(@PathVariable Long doctorId) {
        doctorAccountService.resetPassword(doctorId);
        return Result.success("密码已重置为默认密码:123456");
    }

    /**
     * 查询医生详情
     */
    @GetMapping("/{doctorId}")
    public Result<DoctorAccountDTO> getDoctorDetail(@PathVariable Long doctorId) {
        DoctorAccountDTO doctor = doctorAccountService.getDoctorById(doctorId);
        return Result.success(doctor);
    }

    /**
     * 审批医生修改bio的申请
     */
    @PostMapping("/bio/review/{requestId}")
    public Result<String> review(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason
    ) {
        try {
            doctorAccountService.reviewRequest(requestId, approved, reason);
            return Result.success(approved ? "审核通过" : "已拒绝");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审批的医生 bio 修改申请列表
     */
    @GetMapping("/bio/pending")
    public Result<List<DoctorBioUpdateRequest>> getPendingBioRequests() {
        return Result.success(doctorAccountService.getPendingBioRequests());
    }

    /**
     * 获取单条申请详情
     */
    @GetMapping("/bio/{requestId}")
    public Result<DoctorBioUpdateRequest> getBioRequestDetail(@PathVariable Long requestId) {
        return Result.success(doctorAccountService.getBioRequestDetail(requestId));
    }
}