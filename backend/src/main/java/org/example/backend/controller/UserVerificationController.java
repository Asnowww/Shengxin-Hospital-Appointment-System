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

    @Value("${file.upload-dir:uploads/verifications/}")
    private String uploadDir;

    /**
     * 用户提交认证信息
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
            if (userId == null) {
                return Result.error(401, "无效的登录凭证");
            }

            // 2️⃣ 校验文件
            if (file.isEmpty()) {
                return Result.error("上传失败：文件为空");
            }



            // 3️⃣ 确保上传目录存在
            File dir = new File(uploadDir);
            System.out.println("目录存在吗: " + dir.exists());
            System.out.println("尝试创建: " + dir.mkdirs());
            if (!dir.exists() && !dir.mkdirs()) {
                return Result.error("上传失败：无法创建目录");
            }

            // 4️⃣ 提取安全的扩展名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            // 5️⃣ 生成随机安全文件名
            String safeFileName = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, safeFileName);

            // 6️⃣ 防止路径遍历攻击
            String canonicalPath = dest.getCanonicalPath();
            String canonicalDir = dir.getCanonicalPath();
            if (!canonicalPath.startsWith(canonicalDir + File.separator)) {
                return Result.error("非法文件路径");
            }

            // 7️⃣ 保存文件
            file.transferTo(dest);

            // 8️⃣ 构造访问 URL（之后会映射到静态目录）
            String docUrl = "/static/verifications/" + safeFileName;

            // 9️⃣ 保存认证信息到数据库
            UserVerification verification = new UserVerification();
            verification.setUserId(userId);
            verification.setIdentityType(identityType);
            verification.setIdNumber(idNumber);
            verification.setDocUrl(docUrl);
            verification.setStatus("pending");

            verificationService.save(verification);

            return Result.success("上传成功，待审核", docUrl);

        } catch (IOException e) {
            return Result.error("上传异常：" + e.getMessage());
        } catch (SecurityException e) {
            return Result.error("安全检查失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("上传或提交失败：" + e.getMessage());
        }
    }

    /**
     * 管理员审核认证
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
     * 查询当前用户认证状态
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
