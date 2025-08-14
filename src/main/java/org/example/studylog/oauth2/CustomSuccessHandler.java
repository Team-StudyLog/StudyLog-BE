package org.example.studylog.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.entity.user.User;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.TokenService;
import org.example.studylog.util.CookieUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;

    public CustomSuccessHandler(JWTUtil jwtUtil, TokenService tokenService,
                                OAuth2AuthorizedClientService authorizedClientService,
                                UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.authorizedClientService = authorizedClientService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String oauthId = customUserDetails.getName();

        // registrationId (예: "google", "kakao")
        String registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication)
                .getAuthorizedClientRegistrationId();

        // 이제 authorizedClientService에서 클라이언트 정보를 가져올 수 있음
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                registrationId,
                oauthId
        );

        // Refresh Token 가져오기
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        if (refreshToken != null) {
            // DB에서 사용자 조회 후 Refresh Token 업데이트
            User user = userRepository.findByOauthId(oauthId);
            if (user != null) {
                user.setRefreshToken(refreshToken.getTokenValue());
                userRepository.save(user);
            }
        }

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
        response.sendRedirect("http://localhost:8080/signup");

    }

}
