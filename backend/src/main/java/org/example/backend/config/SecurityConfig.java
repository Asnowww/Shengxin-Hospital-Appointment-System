package org.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    /**
     * Spring Security 主过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ 关键：让 Security 使用下面定义的 CORS 规则
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 开发阶段关闭 CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 权限策略（开发阶段全放行）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/captcha/**",
                                "/public/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )

                // 关闭默认认证方式
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * ✅ CORS 核心配置（Spring Security 专用）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ⚠️ allowCredentials(true) 时，必须使用 allowedOriginPatterns
        config.setAllowedOriginPatterns(List.of(
                "https://localhost:5173",
                "https://127.0.0.1:5173",
                "https://10.*.*.*:5173",
                "https://192.168.*.*:5173"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        // 如果前端使用 Cookie / Authorization
        config.setAllowCredentials(true);

        // 缓存预检请求 1 小时
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
