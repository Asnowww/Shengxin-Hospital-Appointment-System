package org.example.backend.controller;

import org.example.backend.dto.DoctorAccountDTO;
import org.example.backend.dto.DoctorQueryDTO;
import org.example.backend.dto.PageResult;
import org.example.backend.pojo.DoctorBioUpdateRequest;
import org.example.backend.service.DoctorAccountService;
import org.example.backend.service.AuditLogService;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.dto.Result;
import org.example.backend.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理员 - 医生账号管理控制器
 */
@RestController
@RequestMapping("/api/admin/doctors")
public class AdminDoctorAccountController {

    @Autowired
    private DoctorAccountService doctorAccountService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 查询医生账号列表(支持筛选)
     */
    @GetMapping("/list")
    public Result<PageResult<DoctorAccountDTO>> getDoctorList(DoctorQueryDTO queryDTO) {
        return Result.success(doctorAccountService.getDoctorList(queryDTO));
    }

    /**
     * 新增医生账号
     */
    @PostMapping("/add")
    public Result<String> addDoctor(@RequestBody DoctorAccountDTO doctorDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
        doctorAccountService.addDoctor(doctorDTO);

        recordAudit(authHeader, tokenParam, request,
                "create", "doctor", null,
                "管理员创建医生账号 username=" + doctorDTO.getUsername());
        return Result.success("医生账号创建成功");
    }

    /**
     * 修改医生信息
     */
    @PutMapping("/update/{doctorId}")
    public Result<String> updateDoctor(
            @PathVariable Long doctorId,
            @RequestBody DoctorAccountDTO doctorDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
        doctorDTO.setDoctorId(doctorId);
        doctorAccountService.updateDoctor(doctorDTO);

        recordAudit(authHeader, tokenParam, request,
                "update", "doctor", doctorId,
                "管理员更新医生信息 username=" + doctorDTO.getUsername());
        return Result.success("医生信息更新成功");
    }

    /**
     * 修改医生状态 (在职 / 休假 / 退休)
     */
    @PutMapping("/status/{doctorId}")
    public Result<String> updateDoctorStatus(
            @PathVariable Long doctorId,
            @RequestParam String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
        doctorAccountService.updateDoctorStatus(doctorId, status);
        String msg = "verified".equals(status) ? "启用成功" : "禁用成功";

        recordAudit(authHeader, tokenParam, request,
                "update", "doctor", doctorId,
                "管理员修改医生账号状态=" + status);
        return Result.success("医生状态已更新");
    }

    /**
     * 重置医生密码
     */
    @PutMapping("/reset-password/{doctorId}")
    public Result<String> resetPassword(@PathVariable Long doctorId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
        doctorAccountService.resetPassword(doctorId);

        recordAudit(authHeader, tokenParam, request,
                "update", "doctor", doctorId,
                "管理员重置医生密码");
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
     * 停用/启用医生账号
     */
    @PutMapping("/update/account_status/{doctorId}")
    public Result<String> setDoctorAccountStatus(
            @PathVariable Long doctorId,
            @RequestParam String account_status) {
        try {
            doctorAccountService.updateDoctorAccountStatus(doctorId, account_status);
            return Result.success("修改账号状态成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批医生修改bio的申请
     */
    @PostMapping("/bio/review/{requestId}")
    public Result<String> review(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
        try {
            doctorAccountService.reviewRequest(requestId, approved, reason);

            recordAudit(authHeader, tokenParam, request,
                    approved ? "approve" : "reject",
                    "doctor",
                    requestId,
                    "管理员审核医生Bio修改，结果=" + approved + " reason=" + reason);
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

    private void recordAudit(String authHeader, String tokenParam, HttpServletRequest request,
            String action, String resourceType, Long resourceId, String message) {
        try {
            Long userId = tokenUtil.resolveUserIdFromToken(
                    tokenUtil.extractToken(authHeader, tokenParam));
            AuditLogCreateDTO dto = new AuditLogCreateDTO();
            dto.setAction(action);
            dto.setResourceType(resourceType);
            dto.setResourceId(resourceId);
            dto.setMessage(message);
            dto.setIp(request != null ? request.getRemoteAddr() : null);
            auditLogService.recordLog(userId, dto);
        } catch (Exception ignored) {
        }
    }
}