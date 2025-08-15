package org.example.studylog.controller.jwt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.studylog.dto.ResponseDTO;
import org.example.studylog.dto.oauth.TokenDTO;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.service.TokenService;
import org.example.studylog.util.CookieUtil;
import org.example.studylog.util.ResponseUtil;
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

    @Operation(summary = "AccessToken 재발급 API",
            parameters = {
                @Parameter(
                        in = ParameterIn.COOKIE,
                        name = "refresh",
                        required = true,
                        description = "리프레시 토큰"
                )
            })
    @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        name = "TokenResponseDTO",
                        implementation = TokenDTO.ResponseDTO.class
            ))
    )
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
