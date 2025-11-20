package org.example.backend.service.impl;

import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.backend.service.CaptchaService;
import org.example.backend.util.EmailAPI;
import org.example.backend.util.EmailTemplateEnum;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    @Resource
    private EmailAPI emailAPI;

    @Override
    public boolean sendCaptcha(String email) {
        String key = "login:email:captcha:" + email;
        return sendMailCaptcha(key, EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML);
    }

    private boolean sendMailCaptcha(String key, EmailTemplateEnum template) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(key);

        String lastSendTimestamp = hashOps.get("lastSendTimestamp");
        String sendCount = hashOps.get("sendCount");

        if (StringUtils.isNotBlank(sendCount) && Integer.parseInt(sendCount) >= 5) {
            hashOps.expire(24, TimeUnit.HOURS);
            throw new RuntimeException("验证码请求太频繁（每天最多5次）");
        }

        if (StringUtils.isNotBlank(lastSendTimestamp)) {
            long lastSendTime = Long.parseLong(lastSendTimestamp);
            if ((System.currentTimeMillis() - lastSendTime) < 60_000) {
                throw new RuntimeException("验证码请求太频繁（每分钟最多1次）");
            }
        }

        int newSendCount = StringUtils.isNotBlank(sendCount) ? Integer.parseInt(sendCount) + 1 : 1;
        String captcha = RandomStringUtils.randomNumeric(6);

        // 同步等待邮件发送
        try {
            Boolean sendResult = sendCaptchaMail(key, captcha, template).get(10, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(sendResult)) {
                throw new RuntimeException("邮件发送失败");
            }
        } catch (Exception e) {
            log.error("发送验证码邮件失败", e);
            throw new RuntimeException("验证码发送失败，请稍后重试");
        }

        // 邮件发送成功后才保存到 Redis
        Map<String, String> payload = new HashMap<>(3);
        payload.put("captcha", captcha);
        payload.put("lastSendTimestamp", String.valueOf(System.currentTimeMillis()));
        payload.put("sendCount", String.valueOf(newSendCount));
        hashOps.putAll(payload);
        hashOps.expire(5, TimeUnit.MINUTES);

        return true;
    }


    private CompletableFuture<Boolean> sendCaptchaMail(String hashKey, String captcha, EmailTemplateEnum template) {
        String[] parts = hashKey.split(":");

        // Redis key 格式: login:email:captcha:用户邮箱
        if (parts.length >= 4 && "captcha".equals(parts[2])) {
            String toEmail = parts[parts.length - 1];
            log.info("发送验证码邮件到: {}, 验证码: {}", toEmail, captcha);
            return emailAPI.sendHtmlEmail(
                    template.getSubject(),
                    template.set(captcha),
                    toEmail
            );
        }

        log.error("Redis key 格式错误: {}, 无法提取邮箱地址", hashKey);
        return CompletableFuture.completedFuture(false);
    }


    @Override
    public boolean sendPasswordResetCaptcha(String email) {
        String key = "login:email:captcha:" + email;
        return sendMailCaptcha(key, EmailTemplateEnum.PASSWORD_RESET_CODE_EMAIL_HTML);
    }


    @Override
    public void createGraphCaptcha(HttpServletResponse response) throws IOException {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(SpecCaptcha.TYPE_DEFAULT);

        String code = captcha.text().toLowerCase();
        String captchaId = UUID.randomUUID().toString();
        String redisKey = CAPTCHA_KEY_PREFIX + "graph:" + captchaId;

        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        response.setHeader("Captcha-Id", captchaId);
        response.setContentType("image/png");
        captcha.out(response.getOutputStream());
    }

    @Override
    public boolean verifyGraphCaptcha(String captchaId, String inputCode) {
        if (StringUtils.isAnyBlank(captchaId, inputCode)) {
            return false;
        }

        String key = CAPTCHA_KEY_PREFIX + "graph:" + captchaId;
        String correctCode = stringRedisTemplate.opsForValue().get(key);

        if (correctCode == null) {
            return false;
        }

        stringRedisTemplate.delete(key);
        return correctCode.equalsIgnoreCase(inputCode);
    }
}