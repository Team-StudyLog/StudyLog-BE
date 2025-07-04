package org.example.studylog.config;

import org.example.studylog.jwt.JWTFilter;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.oauth2.CustomFailureHandler;
import org.example.studylog.oauth2.CustomSuccessHandler;
import org.example.studylog.service.oauth.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final JWTUtil jwtUtil;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil, CustomFailureHandler customFailureHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.customFailureHandler = customFailureHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        // csrf disable
        http
                .csrf((auth) -> auth.disable());

        // Form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        // HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        // JWTFilter 추가
        http
                .addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        // oauth2

        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((UserInfoEndpointConfig) -> UserInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                        .failureHandler(customFailureHandler)
                );

        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login", "/main").permitAll()
                        .anyRequest().authenticated());

        // 세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        return http.build();
    }
}
