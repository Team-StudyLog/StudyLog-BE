package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.MainService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final MainService mainService;
    private final UserRepository userRepository;

    @GetMapping("/main")
    public ResponseEntity<?> getMainPage(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @RequestParam(required = false) String code) {

        // code 파라미터가 있으면 코드로 조회, 없으면 기존 로직
        if (code != null && !code.trim().isEmpty()) {
            return getMainPageByCode(code);  // private 메서드 호출
        }

        // 기존 로직 (인증된 사용자)
        try {
            log.info("메인 페이지 조회 요청: 사용자={}", currentUser.getName());

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "접근 권한이 없습니다.", false);
            }

            Object mainPageData = mainService.getMainPageData(user);

            log.info("메인 페이지 조회 성공: 사용자={}", currentUser.getName());

            return ResponseUtil.buildResponse(200, "메인 페이지 조회에 성공하였습니다.", mainPageData);

        } catch (Exception e) {
            log.error("메인 페이지 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다. 다시 접속해주세요.", null);
        }
    }

    // 동일 엔드포인트에서 쿼리 지원을 위해 private 메서드로 변경
    private ResponseEntity<?> getMainPageByCode(String code) {
        try {
            log.info("코드로 메인 페이지 조회 요청: code={}", code);

            User user = userRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 코드입니다."));

            Object mainPageData = mainService.getMainPageData(user);

            log.info("코드로 메인 페이지 조회 성공: code={}, 사용자={}", code, user.getOauthId());

            return ResponseUtil.buildResponse(200, "메인 페이지 조회에 성공하였습니다.", mainPageData);

        } catch (IllegalArgumentException e) {
            log.warn("코드로 메인 페이지 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(404, e.getMessage(), null);

        } catch (Exception e) {
            log.error("코드로 메인 페이지 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다. 다시 접속해주세요.", null);
        }
    }
}