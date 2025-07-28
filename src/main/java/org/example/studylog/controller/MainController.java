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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final MainService mainService;
    private final UserRepository userRepository;

    @GetMapping("/main")
    public ResponseEntity<?> getMainPage(@AuthenticationPrincipal CustomOAuth2User currentUser) {
        try {
            log.info("메인 페이지 조회 요청: 사용자={}", currentUser.getName());

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "접근 권한이 없습니다.", false);
            }

            // MainService에서 메인 페이지 데이터를 조회
            Object mainPageData = mainService.getMainPageData(user);

            log.info("메인 페이지 조회 성공: 사용자={}", currentUser.getName());

            return ResponseUtil.buildResponse(200, "메인 페이지 조회에 성공하였습니다.", mainPageData);

        } catch (Exception e) {
            log.error("메인 페이지 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다. 다시 접속해주세요.", null);
        }
    }
}