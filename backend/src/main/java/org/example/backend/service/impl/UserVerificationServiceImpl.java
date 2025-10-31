package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.config.FileStorageProperties;
import org.example.backend.mapper.UserVerificationMapper;
import org.example.backend.pojo.UserVerification;
import org.example.backend.service.UserVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;

@Service
public class UserVerificationServiceImpl
        extends ServiceImpl<UserVerificationMapper, UserVerification>
        implements UserVerificationService {

    @Autowired
    private FileStorageProperties fileStorageProperties;

    /**
     * 用户提交认证
     */
    @Override
    public boolean submitVerification(Long userId, String identityType, String idNumber, MultipartFile file) throws Exception {

        // 获取配置的上传路径
        String uploadDir = fileStorageProperties.getUploadDir();
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, filename);
        file.transferTo(dest);

        // 生成可访问的 URL（局域网/公网都行）
        String ip = InetAddress.getLocalHost().getHostAddress();
        String fileUrl = "http://" + ip + ":8080" + fileStorageProperties.getAccessPath() + filename;

        // 构造认证对象
        UserVerification v = new UserVerification();
        v.setUserId(userId);
        v.setIdentityType(identityType);
        v.setIdNumber(idNumber);
        v.setDocUrl(fileUrl);
        v.setStatus("pending");
        v.setCreatedAt(LocalDateTime.now());

        return this.save(v);
    }

    /**
     * 审核认证：管理员操作
     * @param verificationId 认证记录ID
     * @param reviewerId 审核人ID
     * @param approved 是否通过
     * @param reason 拒绝理由（通过时可为空）
     */
    @Override
    public boolean reviewVerification(Long verificationId, Long reviewerId, boolean approved, String reason) {
        UserVerification v = this.getById(verificationId);
        if (v == null) return false;

        v.setReviewedBy(reviewerId);
        v.setReviewedAt(LocalDateTime.now());
        v.setStatus(approved ? "approved" : "rejected");

        if (!approved) {
            v.setRejectionReason(reason != null ? reason : "未提供理由");
        } else {
            v.setRejectionReason(null); // 清空旧理由
        }


        return this.updateById(v);
    }

    @Override
    public UserVerification getLatestByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(UserVerification::getUserId, userId)
                .orderByDesc(UserVerification::getCreatedAt)
                .last("LIMIT 1")
                .one();
    }
}
