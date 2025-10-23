package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.backend.dto.PatientRegisterParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.CaptchaService;
import org.example.backend.service.PatientService;
import org.example.backend.service.UserService;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/auth")
public class LoginController {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private PatientService patientService;

    @PostMapping("/login")
    @ResponseBody
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginMap) {
        String account = loginMap.get("account");
        String password = loginMap.get("password");
        String roleType = loginMap.get("roleType");

        //验证码
        String captchaId = loginMap.get("captchaId");
        String captchaCode = loginMap.get("captchaCode");


        // ===== 1. 校验图形验证码 =====
        if (StringUtils.isAnyBlank(captchaId, captchaCode)) {
            return new Result<>(400, "请填写验证码", null);
        }

        boolean captchaValid = captchaService.verifyGraphCaptcha(captchaId, captchaCode);
        if (!captchaValid) {
            return new Result<>(401, "验证码错误或已过期", null);
        }


        if (account == null || password == null) {
            return new Result<>(400, "账号或密码不能为空", null);
        }

        // 查询用户：用户名或邮箱或手机号
        User user = userService.getOne(
                new QueryWrapper<User>()
                        .lambda()
                        .eq(User::getUsername, account)
                        .or()
                        .eq(User::getEmail, account)
                        .or()
                        .eq(User::getPhone, account));

        if (user == null) {
            return new Result<>(404, "用户不存在", null);
        }

        // 校验密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return new Result<>(401, "密码错误", null);
        }

        if (StringUtils.isBlank(roleType)) {
            return new Result<>(400, "角色类型不能为空", null);
        }

        if (!roleType.equalsIgnoreCase(user.getRoleType())) {
            return new Result<>(403, "账户或密码错误", null);
        }

        // 生成 token（UUID）
        String token = UUID.randomUUID().toString();

        // 保存 token 到 Redis，设置过期时间（7 天）
        // 1) token -> userId（校验用）
        stringRedisTemplate.opsForValue().set("auth:token:" + token, String.valueOf(user.getUserId()), 7,
                TimeUnit.DAYS);
        // 2) userId -> token（可用于单点登录或踢下线场景，按需使用）
        stringRedisTemplate.opsForValue().set("auth:user-token:" + user.getUserId(), token, 7, TimeUnit.DAYS);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("account", user.getUserId());
        data.put("password", ""); // 返回空密码
        data.put("token", token);
        data.put("roleType",roleType);
        data.put("email", user.getEmail());
        data.put("status", user.getStatus());

        // 如果是患者，查询 patientId 并返回
        if ("patient".equalsIgnoreCase(roleType)) {
            Patient patient = patientService.getOne(
                    new QueryWrapper<Patient>().lambda()
                            .eq(Patient::getUserId, user.getUserId())
            );
            if (patient != null) {
                data.put("patientId", patient.getPatientId());
            }
        }

        return new Result<>(200, "登录成功", data);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    @ResponseBody
    public Result<Void> sendEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return new Result<>(400, "邮箱不能为空", null);
        }

        // 先检查邮箱是否已被注册
        User existUser = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (existUser != null) {
            return new Result<>(409, "该邮箱已被注册，请直接登录", null);
        }

        // 发送验证码（自动包含限流、Redis存储等逻辑）
        try {
            captchaService.sendCaptcha(email);
            return new Result<>(200, "验证码已发送，请查收邮箱", null);
        } catch (RuntimeException e) {
            // 捕获限流异常
            return new Result<>(429, e.getMessage(), null);
        }
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/register/patient")
    @ResponseBody
    public Result<Void> registerPatient(@RequestBody PatientRegisterParam param) {
        String email = param.getEmail();
        String emailCode = param.getEmailCode();

        // 验证码校验
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate
                .boundHashOps("login:email:captcha:" + email);
        String code = hashOps.get("captcha");

        System.out.println("Redis key: login:email:captcha:" + email);
        System.out.println("Redis value: " + stringRedisTemplate.opsForHash().get("login:email:captcha:" + email, "captcha"));


        if (code == null)
            return new Result<>(400, "验证码已过期或不存在", null);
        if (!Objects.equals(code, emailCode))
            return new Result<>(400, "验证码错误", null);

        try {
            // 调用 Service 处理注册（包括 User + Patient 保存）
            userService.registerPatient(param);

            // 清除验证码
            stringRedisTemplate.delete("login:email:captcha:" + email);

            return new Result<>(200, "注册成功", null);
        } catch (RuntimeException e) {
            return new Result<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new Result<>(500, "注册失败，请稍后重试", null);
        }
    }
}