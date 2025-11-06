package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.AdminUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.User;
import org.example.backend.service.UserService;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Resource
    private TokenUtil tokenUtil; // 注入工具类

    @Resource
    private UserService userService;

    @GetMapping("/profile")
    public Result<Map<String, Object>> getAdminProfileByToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam) {

        // 从 header 或参数中提取 token
        String token = tokenUtil.extractToken(authorization, tokenParam);
        Long userId = tokenUtil.resolveUserIdFromToken(token);
        if (userId == null) {
            return new Result<>(401, "未登录或 token 无效", null);
        }

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return new Result<>(404, "用户不存在", null);
            }

            // 确认用户角色为管理员
            if (!"admin".equalsIgnoreCase(user.getRoleType())) {
                return new Result<>(403, "该用户不是管理员角色", null);
            }

            // 仅返回管理员所需信息
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());

            return new Result<>(200, "获取管理员信息成功", data);
        } catch (Exception e) {
            return new Result<>(500, "获取管理员信息失败：" + e.getMessage(), null);
        }
    }

    @PutMapping("/profile/update")
    public Result<Void> updateAdminProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestBody AdminUpdateParam param) {

        // 提取 token 并解析 userId
        String token = tokenUtil.extractToken(authorizationHeader, tokenParam);
        Long userId = tokenUtil.resolveUserIdFromToken(token);
        if (userId == null) {
            return new Result<>(401, "无效的登录凭证", null);
        }

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return new Result<>(404, "用户不存在", null);
            }
            if (!"admin".equalsIgnoreCase(user.getRoleType())) {
                return new Result<>(403, "该用户不是管理员角色", null);
            }

            // 仅允许修改 phone 和 email
            if (param.getPhone() != null && !param.getPhone().trim().isEmpty()) {
                user.setPhone(param.getPhone());
            }
            if (param.getEmail() != null && !param.getEmail().trim().isEmpty()) {
                user.setEmail(param.getEmail());
            }

            userService.updateById(user);
            return new Result<>(200, "管理员信息更新成功", null);

        } catch (IllegalArgumentException e) {
            return new Result<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new Result<>(500, "更新管理员信息失败：" + e.getMessage(), null);
        }
    }

}
