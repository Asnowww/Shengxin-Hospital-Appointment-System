package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.config.FileStorageProperties;
import org.example.backend.mapper.UserMapper;
import org.example.backend.mapper.UserVerificationMapper;
import org.example.backend.pojo.User;
import org.example.backend.pojo.UserVerification;
import org.example.backend.service.UserVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.backend.pojo.UserVerification.VerificationStatus.pending;

@Service
public class UserVerificationServiceImpl
        extends ServiceImpl<UserVerificationMapper, UserVerification>
        implements UserVerificationService {

    private final FileStorageProperties fileStorageProperties;
    private final UserMapper userMapper;

    public UserVerificationServiceImpl(FileStorageProperties fileStorageProperties,
                                       UserMapper userMapper) {
        this.fileStorageProperties = fileStorageProperties;
        this.userMapper = userMapper;
    }
    /**
     * 用户提交认证
     */
    @Override
    @Transactional
    public UserVerification submitVerification(Long userId, String identityType, String idNumber, MultipartFile file) throws Exception {

        // 获取配置的上传路径
        String uploadDir = fileStorageProperties.getUploadDir();
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if(originalName != null && originalName.contains(".")){
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = System.currentTimeMillis() + ext; // 只保留数字 + 后缀
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
        v.setStatus(pending);
        v.setCreatedAt(LocalDateTime.now());

        // 保存认证记录
        boolean saved = this.save(v);

        if (saved) {
            // 同步更新用户表状态为 pending
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setStatus("pending");
                userMapper.updateById(user);
            }
        }

        return v;
    }

    /**
     * 审核认证：管理员操作
     * @param verificationId 认证记录ID
     * @param reviewerId 审核人ID
     * @param result 是否通过
     * @param reason 拒绝理由（通过时可为空）
     */
    @Override
    public UserVerification reviewVerification(Long verificationId, Long reviewerId, UserVerification.VerificationStatus result, String reason) {
        UserVerification v = this.getById(verificationId);
        if (v == null) return null;

        v.setReviewedBy(reviewerId);
        v.setReviewedAt(LocalDateTime.now());
        v.setStatus(result);

        if (result == UserVerification.VerificationStatus.rejected) {
            v.setRejectionReason(reason);
        } else {
            v.setRejectionReason(null);
        }

        this.updateById(v);

        // 同步更新用户状态
        User user = userMapper.selectById(v.getUserId());
        if (user != null) {
            user.setStatus(result == UserVerification.VerificationStatus.approved ? "verified" : "pending");
            userMapper.updateById(user);
        }

        return v;
    }

    @Override
    public UserVerification getLatestByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(UserVerification::getUserId, userId)
                .orderByDesc(UserVerification::getCreatedAt)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<Map<String, Object>> getAllPending() {
        List<UserVerification> pendingList = this.lambdaQuery()
                .eq(UserVerification::getStatus, "pending")
                .orderByAsc(UserVerification::getCreatedAt)
                .list();

        return pendingList.stream().map(v -> {
            User user = userMapper.selectById(v.getUserId());
            return v.toMap(user);
        }).collect(Collectors.toList());
    }
}
