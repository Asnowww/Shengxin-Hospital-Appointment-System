package org.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ✅ 所有认证相关接口全部放行
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/captcha/**",
                                "/public/**",
                                "/error")
                        .permitAll()

                        // ✅ 开发阶段：API 全放行
                        .requestMatchers("/api/**").permitAll()

                        // 其他一切
                        .anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ CORS 正确写法（支持 HTTPS + 局域网访问）
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // 使用 allowedOriginPatterns 支持通配符，允许所有局域网 IP
                        .allowedOriginPatterns(
                                "https://localhost:*",
                                "https://127.0.0.1:*",
                                "https://10.*.*.*:*", // 局域网 10.x.x.x
                                "https://192.168.*.*:*", // 局域网 192.168.x.x
                                "https://172.16.*.*:*", // 局域网 172.16.x.x - 172.31.x.x
                                "http://localhost:*", // 开发环境可能用 HTTP
                                "http://127.0.0.1:*")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
