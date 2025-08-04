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
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 제외할 URI 리스트
        // 로그인 완료 직후 접근하는 페이지들, 토큰 재발급 API, 정적 리소스 제외
        return path.equals("/signup")          // 로그인 완료 페이지
                || path.equals("/login-success")   // (예시) 다른 완료 페이지
                || path.equals("/login")
                || path.equals("/")
                || path.equals("/index.html")
                || path.startsWith("/static/")
                || path.startsWith("/.well-known")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.equals("/auth/token-reissue");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
//        if (requestURI.equals("/auth/token-reissue") || requestURI.equals("/")) {
//            filterChain.doFilter(request, response); // 토큰 검사 생략
//            return;
//        }

        // 요청(request)에서 Authorization 헤더 찾기
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if(authorization == null || !authorization.startsWith("Bearer ")){
            log.warn("Authorization 헤더 검증에 실패: REQUEST_URI = {}", requestURI);
            // 해당 필더 종료
            filterChain.doFilter(request, response);
            // 메소드 종료
            return;
        }

        String accessToken = authorization.split(" ")[1];

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
