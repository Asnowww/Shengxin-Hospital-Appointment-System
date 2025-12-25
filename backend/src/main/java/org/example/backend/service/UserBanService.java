package org.example.backend.service;

import jakarta.annotation.Resource;
import org.example.backend.mapper.BannedUserMapper;
import org.example.backend.mapper.PatientMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.BannedUser;
import org.example.backend.pojo.Patient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserBanService {

    @Resource
    private BannedUserMapper bannedUserMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    NotificationEmailService notificationEmailService;

    @Resource
    private PatientMapper patientMapper;

    public boolean isUserBanned(Long userId) {
        BannedUser activeBan = bannedUserMapper.selectActiveByUserId(userId);
        return activeBan != null;
    }

    public BannedUser getActiveBan(Long userId) {
        return bannedUserMapper.selectActiveByUserId(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void banUser(Long patientId, String banType, String reason, int durationWeeks) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }

        Long userId = patient.getUserId();

        BannedUser existingBan = bannedUserMapper.selectActiveByUserId(userId);
        if (existingBan != null) {
            System.out.println("用户 [ID: " + userId + "] 已有生效的禁用记录，跳过重复禁用");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusWeeks(durationWeeks);

        BannedUser bannedUser = new BannedUser();
        bannedUser.setUserId(userId);
        bannedUser.setPatientId(patientId);
        bannedUser.setBanType(banType);
        bannedUser.setBanReason(reason);
        bannedUser.setBanStartTime(now);
        bannedUser.setBanDurationWeeks(durationWeeks);
        bannedUser.setBanEndTime(endTime);
        bannedUser.setIsActive(true);
        bannedUser.setCreatedAt(now);
        bannedUser.setUpdatedAt(now);

        bannedUserMapper.insert(bannedUser);
        userMapper.updateBookingStatus(userId, "disabled");

        notificationEmailService.sendAccountBannedNotification(patient.getPatientId());

        System.out.println("用户已被禁止预约 [" +
                "用户ID: " + userId +
                ", 患者ID: " + patientId +
                ", 类型: " + banType +
                ", 时长: " + durationWeeks + "周" +
                ", 解禁时间: " + endTime + "]");
    }

    @Transactional
    public void unbanUser(Long banId) {
        BannedUser bannedUser = bannedUserMapper.selectById(banId);
        if (bannedUser == null) {
            return;
        }

        bannedUser.setIsActive(false);
        bannedUser.setUpdatedAt(LocalDateTime.now());
        bannedUserMapper.updateById(bannedUser);

        BannedUser otherActiveBan = bannedUserMapper.selectActiveByUserId(bannedUser.getUserId());

        if (otherActiveBan == null) {
            userMapper.updateBookingStatus(bannedUser.getUserId(), "enabled");
            System.out.println("用户已解禁 [用户ID: " + bannedUser.getUserId() +
                    ", 患者ID: " + bannedUser.getPatientId() + "]");
        } else {
            System.out.println("用户还有其他生效的禁用记录，暂不恢复预约权限 [用户ID: " + bannedUser.getUserId() + "]");
        }
    }

    public Long countNoShowInThreeMonths(Long patientId) {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        return bannedUserMapper.countNoShowSince(patientId, threeMonthsAgo);
    }

    public Long countCancelInOneWeek(Long patientId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return bannedUserMapper.countCancelSince(patientId, oneWeekAgo);
    }
}
