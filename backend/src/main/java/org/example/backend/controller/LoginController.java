package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.dto.UserParam;
import org.example.backend.pojo.User;
import org.example.backend.service.CaptchaService;
import org.example.backend.service.UserService;
import org.example.backend.util.IdCardValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.*;

@Controller
@RequestMapping("/auth")
public class LoginController {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CaptchaService captchaService;


    @PostMapping("/login")
    @ResponseBody
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginMap) {
        String account = loginMap.get("account");
        String password = loginMap.get("password");

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
                        .eq(User::getPhone, account)
        );

        if (user == null) {
            return new Result<>(404, "用户不存在", null);
        }

        // 校验密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return new Result<>(401, "密码错误", null);
        }

        // 生成 token（UUID）
        String token = UUID.randomUUID().toString();

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("account", user.getUserId());
        data.put("password", ""); // 返回空密码
        data.put("token", token);
        data.put("email", user.getEmail());

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
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
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
    @PostMapping("/register")
    @ResponseBody
    public Result<Void> registerUser(@RequestBody UserParam userParam) {
        String email = userParam.getEmail();
        String emailCode = userParam.getEmailCode();

        // 1. 验证码校验
        BoundHashOperations<String, String, String> hashOps =
                stringRedisTemplate.boundHashOps("login:email:captcha:" + email);
        String code = hashOps.get("captcha");

        if (code == null) {
            return new Result<>(400, "验证码已过期或不存在", null);
        }
        if (!Objects.equals(code, emailCode)) {
            return new Result<>(400, "验证码错误", null);
        }

        // 用户名、邮箱、手机号唯一性检查
        if (userService.findByEmail(email) != null) {
            return new Result<>(409, "该邮箱已被注册，请直接登录", null);
        }
        if (userService.findByUsername(userParam.getUsername()) != null) {
            return new Result<>(409, "用户名已存在，请更换", null);
        }
        if (userService.findByPhone(userParam.getPhone()) != null) {
            return new Result<>(409, "该手机号已注册", null);
        }

        // 校验身份证号格式是否合法
        if (userParam.getIdCard() == null || !IdCardValidator.isValidIdCard(userParam.getIdCard())) {
            return new Result<>(400, "身份证号格式不正确", null);
        }

        // 构建用户对象
        User user = new User();
        BeanUtils.copyProperties(userParam, user);

        if (userParam.getName() == null || userParam.getName().trim().isEmpty()) {
            return new Result<>(400, "姓名不能为空", null);
        }

        // 密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(userParam.getPassword()));

        // 保存用户
        boolean success = userService.register(user);
        if (!success) {
            return new Result<>(500, "注册失败，请稍后重试", null);
        }

        // 清除验证码
        stringRedisTemplate.delete("login:email:captcha:" + email);

        return new Result<>(200, "注册成功", null);
    }

}