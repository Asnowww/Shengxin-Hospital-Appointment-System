package org.example.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.service.CaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.io.IOException;

/**
 * 图形验证码 Controller
 */
@RestController
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    /**
     * 获取图形验证码
     * 前端请求后，会返回验证码图片，同时响应头中包含 Captcha-Id
     */
    @GetMapping("/captcha/graph")
    public void getGraphCaptcha(HttpServletResponse response) throws IOException {
        captchaService.createGraphCaptcha(response);
    }
}
