package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.pojo.UserVerification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserVerificationService extends IService<UserVerification> {

    // 用户提交认证
    UserVerification submitVerification(Long userId, String identityType, String idNumber, MultipartFile file)
            throws Exception;

    // 管理员审核
    UserVerification reviewVerification(Long verificationId, Long reviewerId,
            UserVerification.VerificationStatus result, String reason);

    UserVerification getLatestByUserId(Long userId);

    List<Map<String, Object>> getAllPending();

    // 获取所有认证记录（用于管理端筛选）
    List<Map<String, Object>> getAllVerifications();

}
