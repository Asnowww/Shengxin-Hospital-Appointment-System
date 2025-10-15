package org.example.backend.service.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.backend.service.CaptchaService;
import org.example.backend.util.EmailAPI;
import org.example.backend.util.EmailTemplateEnum;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailAPI emailAPI;

    @Override
    public boolean sendCaptcha(String email) {
        String key = "login:email:captcha:" + email;
        return sendMailCaptcha(key);
    }

    private boolean sendMailCaptcha(String key) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(key);

        // 获取上次发送时间和发送次数
        String lastSendTimestamp = hashOps.get("lastSendTimestamp");
        String sendCount = hashOps.get("sendCount");

        // 限制每天最多发送5次
        if (StringUtils.isNotBlank(sendCount) && Integer.parseInt(sendCount) >= 5) {
            hashOps.expire(24, TimeUnit.HOURS);
            throw new RuntimeException("验证码发送过于频繁，每天最多5次");
        }

        // 限制每分钟只能发送一次
        if (StringUtils.isNotBlank(lastSendTimestamp)) {
            long lastSendTime = Long.parseLong(lastSendTimestamp);
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastSendTime) < 60 * 1000) {
                throw new RuntimeException("验证码发送过于频繁，每分钟最多一次");
            }
        }

        int newSendCount = StringUtils.isNotBlank(sendCount) ? Integer.parseInt(sendCount) + 1 : 1;
        String captcha = RandomStringUtils.randomNumeric(6);

        try {
            sendCaptchaMail(key, captcha);
        } catch (Exception e) {
            throw new RuntimeException("发送验证码邮件失败", e);
        }

        // 存入 Redis
        hashOps.put("captcha", captcha);
        hashOps.put("lastSendTimestamp", String.valueOf(System.currentTimeMillis()));
        hashOps.put("sendCount", String.valueOf(newSendCount));
        hashOps.expire(5, TimeUnit.MINUTES); // 验证码有效期5分钟

        return true;
    }

    private void sendCaptchaMail(String hashKey, String captcha) throws Exception {
        // 判断是否为邮箱验证码
        if ("email".equals(hashKey.split(":")[1])) {
            String toEmail = hashKey.split(":")[3];
            boolean result = emailAPI.sendHtmlEmail(
                    EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.getSubject(),
                    EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.set(captcha),
                    toEmail
            );
            if (!result) {
                throw new RuntimeException("邮件发送失败");
            }
        }
    }
}
