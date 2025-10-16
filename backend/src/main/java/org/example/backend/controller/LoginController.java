package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.dto.UserParam;
import org.example.backend.pojo.User;
import org.example.backend.service.UserService;
import org.example.backend.util.EmailAPI;
import org.springframework.beans.BeanUtils;
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
    private EmailAPI emailAPI;


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

        // 生成 4 位数字验证码
        String code = String.format("%04d", new Random().nextInt(10000));

        // 存入 Redis，5分钟过期
        BoundHashOperations<String, String, String> hashOps =
                stringRedisTemplate.boundHashOps("login:email:captcha:" + email);
        hashOps.put("captcha", code);
        stringRedisTemplate.expire("login:email:captcha:" + email, 5, TimeUnit.MINUTES);

        // 发送邮件
        String subject = "注册验证码";
        String content = "您的注册验证码是：" + code + "，有效期 5 分钟，请勿泄露。";
        boolean send = emailAPI.sendGeneralEmail(subject, content, email);

        if (!send) {
            return new Result<>(500, "验证码发送失败，请稍后重试", null);
        }

        return new Result<>(200, "验证码已发送，请查收邮箱", null);
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

        // 2. 用户名、邮箱、手机号唯一性检查
        if (userService.findByEmail(email) != null) {
            return new Result<>(409, "该邮箱已被注册，请直接登录", null);
        }
        if (userService.findByUsername(userParam.getUsername()) != null) {
            return new Result<>(409, "用户名已存在，请更换", null);
        }

        // 3. 构建用户对象
        User user = new User();
        BeanUtils.copyProperties(userParam, user);

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(null);
        }

        // 4. 密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(userParam.getPassword()));

        // 5. 保存用户（调用 register()）
        boolean success = userService.register(user);
        if (!success) {
            return new Result<>(500, "注册失败，请稍后重试", null);
        }

        // 6. 清除验证码
        stringRedisTemplate.delete("login:email:captcha:" + email);

        return new Result<>(200, "注册成功", null);
    }

}
