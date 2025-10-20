package org.example.backend.service.impl;

import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
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

    // 图形验证码
    @Override
    public void createGraphCaptcha(HttpServletResponse response) throws IOException {
        // 1. 创建验证码对象
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(SpecCaptcha.TYPE_DEFAULT);

        // 2. 获取验证码文本
        String code = captcha.text().toLowerCase();

        // 3. 生成唯一标识（给前端）
        String captchaId = UUID.randomUUID().toString();

        // 4. Redis 中的 key（内部使用）
        String redisKey = "captcha:graph:" + captchaId;

        // 5. 存入 Redis，5 分钟有效
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 6. 将 captchaId 通过响应头传给前端
        response.setHeader("Captcha-Id", captchaId);
        response.setContentType("image/png");

        // 7. 输出图片流
        captcha.out(response.getOutputStream());
    }

    /**
     * 校验图形验证码
     */
    @Override
    public boolean verifyGraphCaptcha(String captchaId, String inputCode) {
        if (StringUtils.isAnyBlank(captchaId, inputCode)) {
            return false;
        }

        // 拼出 redis 中的 key
        String key = "captcha:graph:" + captchaId;

        // 获取 redis 中的验证码
        String correctCode = stringRedisTemplate.opsForValue().get(key);

        if (correctCode == null) {
            return false; // 过期或不存在
        }

        // 删除验证码，防止重复验证
        stringRedisTemplate.delete(key);

        // 比较验证码（忽略大小写）
        return correctCode.equalsIgnoreCase(inputCode);
    }

}
