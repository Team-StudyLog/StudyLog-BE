package org.example.studylog.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ProfileCheckFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public ProfileCheckFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
            String oauthId = auth.getName();
            User user = userRepository.findByOauthId(oauthId);

            // 사용자의 회원 정보 입력 유무
            boolean isProfileCompleted = user.isProfileCompleted();
            String requestURI = request.getRequestURI();
            String method = request.getMethod();

            // /signup, /users/profile의 PUT 요청은 허용, 그 외는 막음
            if(!isProfileCompleted && !requestURI.startsWith("/signup")&&
                    !(requestURI.equals("/users/profile") && method.equalsIgnoreCase("POST"))) {
                log.info("ProfileCheckFilter로 인해 /signup으로 리다이렉션");
                response.sendRedirect("/signup");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
