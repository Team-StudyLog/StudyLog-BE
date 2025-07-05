package org.example.studylog.controller.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.studylog.jwt.JWTUtil;
import org.example.studylog.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReissueController {

    private final JWTUtil jwtUtil;

    public ReissueController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){

        // 쿠키에서 refresh 토큰 얻기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("refresh")){
                refresh = cookie.getValue();
            }
        }

        // refresh가 없을 때 BAD_REQUEST
        if(refresh == null){
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // refresh 토큰의 만료 여부 확인
        try{
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e){
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh 인지 확인
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals("refresh")){
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 새로운 access 토큰 발급
        String oauthId = jwtUtil.getOauthId(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt("access", oauthId, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", oauthId, role, 86400000L);

        response.addCookie(CookieUtil.createCookie("access", newAccess));
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
