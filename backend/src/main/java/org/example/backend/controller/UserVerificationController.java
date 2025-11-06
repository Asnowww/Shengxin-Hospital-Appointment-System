package org.example.backend.controller;

import org.example.backend.dto.Result;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.User;
import org.example.backend.pojo.UserVerification;
import org.example.backend.service.UserVerificationService;
import org.example.backend.util.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/verifications")
public class UserVerificationController {

    private final UserVerificationService verificationService;
    private final TokenUtil tokenUtil;
    private final UserMapper userMapper;

    public UserVerificationController(UserVerificationService verificationService,
                                      TokenUtil tokenUtil,
                                      UserMapper userMapper) {
        this.verificationService = verificationService;
        this.tokenUtil = tokenUtil;
        this.userMapper = userMapper;
    }

    // 从 application.properties 或 application-local.properties 读取上传目录
    @Value("${file.upload-dir:uploads/verifications/}")
    private String uploadDir;

    /**
     * 用户提交认证信息（含图片）
     * token 来源：Header 或 参数，统一解析 userId
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
            String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) return Result.error(401, "无效的登录凭证");

            UserVerification verification = verificationService.submitVerification(userId, identityType, idNumber, file);
            User user = userMapper.selectById(userId);
            Map<String, Object> data = verification.toMap(user);

            return Result.success("上传成功，待审核", data);
        } catch (Exception e) {
            return Result.error("上传或提交失败：" + e.getMessage());
        }
    }

    /**
     * 审核接口（管理员）
     */
    @PostMapping("/review")
    public Result<Map<String, Object>> reviewVerification(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestParam("verificationId") Long verificationId,
            @RequestParam("result") UserVerification.VerificationStatus result,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        try {
            String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
            Long reviewerId = tokenUtil.resolveUserIdFromToken(token);
            if (reviewerId == null) return Result.error(401, "无效的管理员凭证");

            UserVerification updated = verificationService.reviewVerification(verificationId, reviewerId, result, reason);
            if (updated == null) return Result.error("审核失败：记录不存在或更新失败");

            User user = userMapper.selectById(updated.getUserId());
            Map<String, Object> data = updated.toMap(user);
            String msg = result == UserVerification.VerificationStatus.approved ? "审核通过" : "审核拒绝";

            return Result.success(msg, data);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的认证状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) return Result.error(401, "无效的登录凭证");

            UserVerification verification = verificationService.getLatestByUserId(userId);
            if (verification == null) return Result.error("未找到认证记录");

            User user = userMapper.selectById(userId);
            return Result.success("查询成功", verification.toMap(user));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getAllPending() {
        try {
            List<Map<String, Object>> data = verificationService.getAllPending();
            return Result.success("查询成功", data);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
