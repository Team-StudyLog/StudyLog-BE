package org.example.studylog.controller.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.service.TokenService;
import org.example.studylog.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    public AuthController(JWTUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @PostMapping("/token-reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){

        // 쿠키에서 refresh 토큰 얻기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("refresh")){
                refresh = cookie.getValue();
            }
        }

        // 리프레시 토큰 검증
        tokenService.validateRefreshToken(refresh);

        // 새로운 access 토큰 발급
        String oauthId = jwtUtil.getOauthId(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt("access", oauthId, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", oauthId, role, 86400000L);

        // Refresh DB에 기존의 Refresh 토큰 삭제 후 새로운 Refresh 토큰 저장
        tokenService.replaceRefreshToken(refresh, newRefresh, oauthId, 86400000L);

        response.addCookie(CookieUtil.createCookie("access", newAccess));
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
