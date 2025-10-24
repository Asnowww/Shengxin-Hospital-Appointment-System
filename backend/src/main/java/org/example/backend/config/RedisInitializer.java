package org.example.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 项目启动时初始化开发环境预设 token
 */
@Component
public class RedisInitializer implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始初始化开发环境 Redis token...");

        // 管理员测试 token
        redisTemplate.opsForValue().set("auth:token:test-admin-token-001", "1");
        redisTemplate.opsForValue().set("auth:user-token:1", "test-admin-token-001");

        // 患者测试 token
        redisTemplate.opsForValue().set("auth:token:test-patient-token-001", "2");
        redisTemplate.opsForValue().set("auth:user-token:2", "test-patient-token-001");

        System.out.println("""
                Redis token 初始化完成！
                患者账户 p 的 token 为：test-patient-token-001
                管理员账户 a 的 token 为：token:test-admin-token-001""");
    }
}
