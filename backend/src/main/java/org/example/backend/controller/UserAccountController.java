package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.pojo.User;
import org.example.backend.service.CaptchaService;
import org.example.backend.service.UserService;
import org.example.backend.util.EmailAPI;
import org.example.backend.util.EmailTemplateEnum;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/user")
public class UserAccountController {

    @Resource
    private UserService userService;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailAPI emailAPI;

    /**
     * 发送修改密码验证码
     */
    @PostMapping("/sendPasswordResetCode")
    public Result<Void> sendPasswordResetCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return new Result<>(400, "邮箱不能为空", null);
        }

        User user = userService.getOneByEmail(email);
        if (user == null) {
            return new Result<>(404, "该邮箱未注册", null);
        }

        try {
            captchaService.sendPasswordResetCaptcha(email);
            return new Result<>(200, "验证码已发送，请查收邮箱", null);
        } catch (RuntimeException e) {
            return new Result<>(429, e.getMessage(), null);
        }
    }


    /**
     * 验证邮箱验证码接口
     */
    @PostMapping("/verifyEmailCode")
    public Result<Void> verifyEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String captcha = body.get("captcha");
        if (email == null || captcha == null) {
            return new Result<>(400, "邮箱、验证码不能为空", null);
        }
        // 验证验证码
        String redisKey = "login:email:captcha:" + email;
        String correctCaptcha = stringRedisTemplate.<String, String>boundHashOps(redisKey).get("captcha");

        if (correctCaptcha == null || !correctCaptcha.equals(captcha)) {
            return new Result<>(401, "验证码错误或已过期", null);
        }
        // 删除验证码缓存
//        stringRedisTemplate.delete(redisKey);
        return new Result<>(200, "邮箱验证码验证成功", null);
    }

    /**
     * 修改密码接口
     */
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String newPassword = body.get("newPassword");
        String captcha = body.get("captcha");

        if (email == null || newPassword == null) {
            return new Result<>(400, "邮箱、新密码不能为空", null);
        }

        // 验证验证码
        String redisKey = "login:email:captcha:" + email;
        String correctCaptcha = stringRedisTemplate.<String, String>boundHashOps(redisKey).get("captcha");

        if (correctCaptcha == null || !correctCaptcha.equals(captcha)) {
            return new Result<>(401, "验证码错误或已过期", null);
        }

        // 修改密码
        boolean success = userService.updatePasswordByEmail(email, newPassword);
        if (!success) {
            return new Result<>(500, "修改密码失败", null);
        }

        // 删除验证码缓存
        stringRedisTemplate.delete(redisKey);


        return new Result<>(200, "密码修改成功", null);
    }
}
