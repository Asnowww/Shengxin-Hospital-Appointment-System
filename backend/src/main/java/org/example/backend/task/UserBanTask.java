package org.example.backend.task;

import jakarta.annotation.Resource;
import org.example.backend.mapper.BannedUserMapper;
import org.example.backend.mapper.PatientMapper;
import org.example.backend.pojo.BannedUser;
import org.example.backend.pojo.Patient;
import org.example.backend.service.UserBanService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户禁用相关定时任务
 *
 * 包含三个定时任务：
 * 1. 检查爽约违规（每小时）
 * 2. 检查频繁取消违规（每30分钟）
 * 3. 自动解禁到期用户（每小时）
 */
@Component
public class UserBanTask {

    @Resource
    private UserBanService userBanService;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private BannedUserMapper bannedUserMapper;

    /**
     * 定时任务1：
     * 检查爽约规则：三个月内累计爽约2次，限制预约12周
     *
     * 爽约定义：
     * - 预约成功后就诊当日未至（appointment_status = 'no_show'）
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkNoShowViolations() {
        try {
            System.out.println("开始检查爽约违规用户");
            System.out.println("规则：三个月内累计爽约 ≥ 2次 → 限制预约12周");

            List<Patient> allPatients = patientMapper.selectList(null);

            if (allPatients == null || allPatients.isEmpty()) {
                System.out.println("没有患者数据");
                return;
            }

            System.out.println("共有 " + allPatients.size() + " 个患者需要检查");

            int bannedCount = 0;

            for (Patient patient : allPatients) {
                try {
                    // 检查是否已被禁用（避免重复处理）
                    if (userBanService.isUserBanned(patient.getUserId())) {
                        continue;
                    }

                    // 统计三个月内爽约次数
                    Long noShowCount = userBanService.countNoShowInThreeMonths(patient.getPatientId());

                    // 如果爽约次数 >= 2次，禁止预约12周
                    if (noShowCount != null && noShowCount >= 2) {
                        String reason = String.format("三个月内累计爽约 %d 次，违反预约规则（规则：≥2次爽约限制12周）", noShowCount);

                        userBanService.banUser(
                                patient.getPatientId(),
                                "no_show",  // 禁用类型
                                reason,     // 禁用原因
                                12          // 限制12周
                        );

                        bannedCount++;

                        System.out.println("✘ 用户因爽约被禁用 [" +
                                "患者ID: " + patient.getPatientId() +
                                ", 用户ID: " + patient.getUserId() +
                                ", 爽约次数: " + noShowCount + "次]");
                    }

                } catch (Exception e) {
                    System.err.println("检查患者 [ID: " + patient.getPatientId() + "] 爽约记录失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (bannedCount > 0) {
                System.out.println("爽约检查完成，共禁用 " + bannedCount + " 个用户");
            } else {
                System.out.println("爽约检查完成，无违规用户");
            }

        } catch (Exception e) {
            System.err.println("爽约检查定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务2：
     * 检查多次取消规则：一周内主动取消达3次，限制预约4周
     *
     * 取消定义：
     * - 预约成功后又取消预约（appointment_status = 'cancelled'）
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkFrequentCancelViolations() {
        try {
            System.out.println("开始检查频繁取消违规用户");
            System.out.println("规则：一周内主动取消 ≥ 3次 → 限制预约4周");

            List<Patient> allPatients = patientMapper.selectList(null);

            if (allPatients == null || allPatients.isEmpty()) {
                System.out.println("没有患者数据");
                return;
            }

            System.out.println("共有 " + allPatients.size() + " 个患者需要检查");

            int bannedCount = 0;

            for (Patient patient : allPatients) {
                try {
                    // 检查是否已被禁用（避免重复处理）
                    if (userBanService.isUserBanned(patient.getUserId())) {
                        continue;
                    }

                    // 统计一周内取消次数
                    Long cancelCount = userBanService.countCancelInOneWeek(patient.getPatientId());

                    // 如果取消次数 >= 3次，禁止预约4周
                    if (cancelCount != null && cancelCount >= 3) {
                        String reason = String.format("一周内主动取消预约 %d 次，违反预约规则（规则：≥3次取消限制4周）", cancelCount);

                        userBanService.banUser(
                                patient.getPatientId(),
                                "frequent_cancel",  // 禁用类型
                                reason,             // 禁用原因
                                4                   // 限制4周
                        );

                        bannedCount++;

                        System.out.println("✘ 用户因频繁取消被禁用 [" +
                                "患者ID: " + patient.getPatientId() +
                                ", 用户ID: " + patient.getUserId() +
                                ", 取消次数: " + cancelCount + "次]");
                    }

                } catch (Exception e) {
                    System.err.println("检查患者 [ID: " + patient.getPatientId() + "] 取消记录失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (bannedCount > 0) {
                System.out.println("频繁取消检查完成，共禁用 " + bannedCount + " 个用户 ");
            } else {
                System.out.println("频繁取消检查完成，无违规用户");
            }

        } catch (Exception e) {
            System.err.println("频繁取消检查定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务3：
     * 自动解禁到期的用户
     *
     * 检查所有 is_active = TRUE 且 ban_end_time <= NOW() 的记录
     * 自动标记为已解禁，并恢复用户的预约权限
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void autoUnbanExpiredUsers() {
        try {
            System.out.println("========== 开始自动解禁到期用户 ==========");

            LocalDateTime now = LocalDateTime.now();

            // 查询所有已过期但仍标记为生效的禁用记录
            List<BannedUser> expiredBans = bannedUserMapper.selectExpiredBans(now);

            if (expiredBans == null || expiredBans.isEmpty()) {
                System.out.println("========== 没有需要解禁的用户 ==========");
                return;
            }

            System.out.println("发现 " + expiredBans.size() + " 个到期的禁用记录");

            int unbannedCount = 0;

            for (BannedUser ban : expiredBans) {
                try {
                    // 调用解禁服务
                    userBanService.unbanUser(ban.getBanId());
                    unbannedCount++;

                    System.out.println("✓ 用户已自动解禁 [" +
                            "用户ID: " + ban.getUserId() +
                            ", 患者ID: " + ban.getPatientId() +
                            ", 禁用类型: " + ban.getBanType() +
                            ", 原因: " + ban.getBanReason() +
                            ", 禁用时长: " + ban.getBanDurationWeeks() + "周]");

                } catch (Exception e) {
                    System.err.println("解禁用户失败 [禁用ID: " + ban.getBanId() + "]: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("========== 自动解禁完成，共解禁 " + unbannedCount + " 个用户 ==========");

        } catch (Exception e) {
            System.err.println("自动解禁定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}