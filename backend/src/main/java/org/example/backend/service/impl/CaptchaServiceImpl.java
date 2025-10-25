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
        return sendMailCaptcha(key);
    }

    private boolean sendMailCaptcha(String key) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(key);

        String lastSendTimestamp = hashOps.get("lastSendTimestamp");
        String sendCount = hashOps.get("sendCount");

        if (StringUtils.isNotBlank(sendCount) && Integer.parseInt(sendCount) >= 5) {
            hashOps.expire(24, TimeUnit.HOURS);
            throw new RuntimeException("Email captcha requested too frequently (max 5 per day).");
        }

        if (StringUtils.isNotBlank(lastSendTimestamp)) {
            long lastSendTime = Long.parseLong(lastSendTimestamp);
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastSendTime) < 60_000) {
                throw new RuntimeException("Email captcha requested too frequently (max once per minute).");
            }
        }

        int newSendCount = StringUtils.isNotBlank(sendCount) ? Integer.parseInt(sendCount) + 1 : 1;
        String captcha = RandomStringUtils.randomNumeric(6);

        CompletableFuture<Boolean> sendFuture = sendCaptchaMail(key, captcha);
        sendFuture.whenComplete((result, throwable) -> {
            if (throwable != null || !Boolean.TRUE.equals(result)) {
                log.error("Failed to send captcha email, clearing cache key {}", key, throwable);
                stringRedisTemplate.delete(key);
            }
        });

        Map<String, String> payload = new HashMap<>(3);
        payload.put("captcha", captcha);
        payload.put("lastSendTimestamp", String.valueOf(System.currentTimeMillis()));
        payload.put("sendCount", String.valueOf(newSendCount));
        hashOps.putAll(payload);
        hashOps.expire(5, TimeUnit.MINUTES);

        return true;
    }

    private CompletableFuture<Boolean> sendCaptchaMail(String hashKey, String captcha) {
        String[] parts = hashKey.split(":");
        if (parts.length >= 4 && "email".equals(parts[1])) {
            String toEmail = parts[3];
            return emailAPI.sendHtmlEmail(
                    EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.getSubject(),
                    EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.set(captcha),
                    toEmail
            );
        }
        return CompletableFuture.completedFuture(true);
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
