package org.example.studylog.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.oauth.UserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        //cookie들을 불러온 뒤 access Key에 담긴 쿠키를 찾음
        String accessToken = null;

        Cookie[] cookies = request.getCookies();

        if(cookies != null){
            for (Cookie cookie : cookies) {
                log.debug("요청 쿠키: {}={}", cookie.getName(), cookie.getValue());
                System.out.println(cookie.getName());
                if (cookie.getName().equals("access")) {

                    accessToken = cookie.getValue();
                }
            }
        } else{
            log.debug("요청에 쿠키가 없습니다. (from {} )", request.getRequestURI());
        }


        //Authorization 검증
        if (accessToken == null) {
            log.warn("JWT 토큰이 없음 (token null)");
            System.out.println("token null");
            filterChain.doFilter(request, response);

            return;
        }

        //토큰 만료 여부 확인
        try{
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e){
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // access token 인지 확인
        String category = jwtUtil.getCategory(accessToken);
        if(!category.equals("access")){
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //토큰에서 oauthId role 획득
        String oauthId = jwtUtil.getOauthId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        log.info("JWT 인증 성공: oauthId={}, role={}", oauthId, role);

        //userDTO를 생성하여 값 set
        UserDTO userDTO = new UserDTO();
        userDTO.setOauthId(oauthId);
        userDTO.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
