package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.pojo.UserVerification;
import org.springframework.web.multipart.MultipartFile;

public interface UserVerificationService extends IService<UserVerification> {

    //用户提交认证
    UserVerification submitVerification(Long userId, String identityType, String idNumber, MultipartFile file) throws Exception;

    //管理员审核
    UserVerification reviewVerification(Long verificationId, Long reviewerId, boolean approved, String reason);

    UserVerification getLatestByUserId(Long userId);
}
