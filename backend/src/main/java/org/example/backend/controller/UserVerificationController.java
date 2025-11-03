package org.example.backend.controller;

import org.example.backend.dto.Result;
import org.example.backend.pojo.UserVerification;
import org.example.backend.service.UserVerificationService;
import org.example.backend.util.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/verifications")
public class UserVerificationController {

    private final UserVerificationService verificationService;
    private final TokenUtil tokenUtil;

    public UserVerificationController(UserVerificationService verificationService, TokenUtil tokenUtil) {
        this.verificationService = verificationService;
        this.tokenUtil = tokenUtil;
    }

    @Value("${file.upload-dir:uploads/verifications/}")
    private String uploadDir;

    /**
     * 🧾 用户提交认证信息（带文件）
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitVerification(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam("identityType") String identityType,
            @RequestParam("idNumber") String idNumber,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1️⃣ 获取 userId
            String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error(401, "无效的登录凭证");
            }

            // 2️⃣ 提交认证
            UserVerification verification = verificationService.submitVerification(userId, identityType, idNumber, file);

            Map<String, Object> data = new HashMap<>();
            data.put("verificationId", verification.getVerificationId());
            data.put("userId", verification.getUserId());
            data.put("identityType", verification.getIdentityType());
            data.put("status", verification.getStatus());
            data.put("docUrl", verification.getDocUrl());
            data.put("submittedAt", verification.getCreatedAt().toString());

            return Result.<Map<String, Object>>success("上传成功，待审核", data);
        } catch (Exception e) {
            return Result.error("上传或提交失败：" + e.getMessage());
        }
    }

    /**
     * 🧾 管理员审核认证
     */
    @PostMapping("/review")
    public Result<UserVerification> reviewVerification(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam("verificationId") Long verificationId,
            @RequestParam("approved") boolean approved,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        try {
            // 1️⃣ 解析管理员身份
            String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
            Long reviewerId = tokenUtil.resolveUserIdFromToken(token);
            if (reviewerId == null) {
                return Result.error(401, "无效的管理员凭证");
            }

            // 2️⃣ 调用 service 层执行审核（同时更新 users.status）
            UserVerification updated = verificationService.reviewVerification(verificationId, reviewerId, approved, reason);
            if (updated == null) {
                return Result.error("审核失败：记录不存在或更新失败");
            }

            String message = approved ? "审核通过" : "审核拒绝";
            return Result.success(message, updated);

        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 🧾 查询当前用户认证状态
     */
    @GetMapping("/status")
    public Result<UserVerification> getStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error(401, "无效的登录凭证");
            }

            UserVerification verification = verificationService.getLatestByUserId(userId);
            if (verification == null) {
                return Result.error("未找到认证记录");
            }

            return Result.success("查询成功", verification);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
