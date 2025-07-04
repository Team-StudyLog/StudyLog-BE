package org.example.studylog.jwt;

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

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        //cookie들을 불러온 뒤 Authorization Key에 담긴 쿠키를 찾음
        String authorization = null;

        Cookie[] cookies = request.getCookies();

        if(cookies != null){
            for (Cookie cookie : cookies) {
                log.debug("요청 쿠키: {}={}", cookie.getName(), cookie.getValue());
                System.out.println(cookie.getName());
                if (cookie.getName().equals("Authorization")) {

                    authorization = cookie.getValue();
                }
            }
        } else{
            log.debug("요청에 쿠키가 없습니다. (from {} )", request.getRequestURI());
        }


        //Authorization 헤더 검증
        if (authorization == null) {
            log.warn("JWT 토큰이 없음 (token null)");
            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰
        String token = authorization;


        /*
        // 1. Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        // 2. 헤더가 없거나 "Bearer "로 시작하지 않으면 필터 통과
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더 없음 또는 형식이 잘못됨: {}", authorizationHeader);
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " 이후의 순수 토큰 추출
        String token = authorizationHeader.substring(7);
        */

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {

            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        log.info("JWT 인증 성공: username={}, role={}", username, role);

        //userDTO를 생성하여 값 set
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
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
