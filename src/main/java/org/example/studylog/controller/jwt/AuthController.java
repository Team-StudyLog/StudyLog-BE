package org.example.studylog.controller.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.studylog.dto.oauth.TokenDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.service.TokenService;
import org.example.studylog.util.CookieUtil;
import org.example.studylog.util.ResponseUtil;
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

        TokenDTO tokenDTO = tokenService.reissueAccessToken(refresh);

        // Refresh 토큰은 쿠키로 전달
        response.addCookie(CookieUtil.createCookie("refresh", tokenDTO.getRefreshToken()));

        // Access 토큰, code, isNewUser는 body로 전달
        TokenDTO.ResponseDTO dto = TokenDTO.ResponseDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .code(tokenDTO.getCode())
                .isNewUser(tokenDTO.isNewUser())
                .build();

        return ResponseUtil.buildResponse(200, "토큰 재발급 완료", dto);
    }

}
