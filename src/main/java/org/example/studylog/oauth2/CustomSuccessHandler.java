package org.example.studylog.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.service.TokenService;
import org.example.studylog.util.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${spring.redirectUri}")
    private String redirectUri;

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    public CustomSuccessHandler(JWTUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String oauthId = customUserDetails.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 토큰 생성
        String refresh = jwtUtil.createJwt("refresh", oauthId, role, 86400000L);

        // refresh 토큰 저장
        tokenService.addRefreshEntity(oauthId, refresh, 86400000L);

        response.addCookie(CookieUtil.createCookie("refresh", refresh));

        // 회원가입 화면으로 리다이렉션(임시: 프론트 로그인 완료 화면으로 변경 예정)
        response.sendRedirect(redirectUri);

    }

}
