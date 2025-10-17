package org.example.backend.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

/**
 * Token 工具类 —— 支持从 Header/参数中提取 Token，
 * 并从 Redis 中解析出对应的用户 ID。
 */
@Component
public class TokenUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 从请求头或请求参数中提取 Token
     * @param authorizationHeader 请求头中的 Authorization
     * @param tokenParam 请求参数中的 token
     * @return 提取出的 token 字符串
     */
    public String extractToken(String authorizationHeader, String tokenParam) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return tokenParam;
    }

    /**
     * 从 Redis 中解析出 userId
     * @param token 登录时生成的 token
     * @return userId 或 null
     */
    public Long resolveUserIdFromToken(String token) {
        if (token == null || token.isEmpty())
            return null;

        String userIdStr = stringRedisTemplate.opsForValue().get("auth:token:" + token);
        if (userIdStr == null)
            return null;

        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
