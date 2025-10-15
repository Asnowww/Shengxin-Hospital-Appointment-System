package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.service.CaptchaService;
import org.example.backend.util.EmailAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    @Resource
    private EmailAPI emailApi;

    @RequestMapping("/getCaptcha")
    public Result<Void> sendCaptcha(@RequestParam("email") String email) {
        try {
            boolean res = captchaService.sendCaptcha(email);
            if (res) {
                return new Result<>(200, "验证码发送成功", null);
            } else {
                return new Result<>(500, "验证码发送失败", null);
            }
        } catch (Exception e) {
            return new Result<>(500, "发送异常: " + e.getMessage(), null);
        }
    }
}
