package com.forerp.erp.config;

import com.forerp.erp.common.jwt.JwtSecurityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtSecurityFilter jwtSecurityFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);// csrf 보호 비활성화(토큰 방식을 사용하므로)
        http.formLogin(AbstractHttpConfigurer::disable); // 기본 로그인 폼 및 HTTP basic 인증 비활성화(우리가 만든 로그인 API 사용)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 관리 정책을 STATELESS로 설정(서버가 사용자 상태를 저장하지 않음)
        http.cors(Customizer.withDefaults()); // WebConfig의 설정을 적용

        // API 경로별 접근 권한 설정
        http.authorizeHttpRequests(auth -> auth
                        // 로그인 API와 초기 사용자 설정 API는 토큰 없이도 접근 허용
                        .requestMatchers("/api/users/login", "/api/users/login/pos", "/api/users/setup/**", "/api/attendance/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}