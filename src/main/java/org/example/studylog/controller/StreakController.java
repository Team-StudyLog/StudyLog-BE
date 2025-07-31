package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.StreakService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StreakController {

    private final StreakService streakService;
    private final UserRepository userRepository;

    @GetMapping("/streak")
    public ResponseEntity<?> getMonthlyStreak(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @RequestParam("year") String year,
            @RequestParam("month") String month) {

        try {
            log.info("월별 스트릭 조회 요청: 사용자={}, year={}, month={}",
                    currentUser.getName(), year, month);

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "접근 권한이 없습니다.", false);
            }

            // 년월 유효성 검증
            validateYearMonth(year, month);

            // 월별 스트릭 데이터 조회
            Object monthlyStreakData = streakService.getMonthlyStreakData(user, year, month);

            log.info("월별 스트릭 조회 성공: 사용자={}, year={}, month={}",
                    currentUser.getName(), year, month);

            return ResponseUtil.buildResponse(200, "스트릭 조회에 성공하였습니다.", monthlyStreakData);

        } catch (IllegalArgumentException e) {
            log.warn("월별 스트릭 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, "잘못된 접근입니다",
                    Map.of("example", e.getMessage()));

        } catch (Exception e) {
            log.error("월별 스트릭 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다. 다시 접속해주세요.", null);
        }
    }

    private void validateYearMonth(String year, String month) {
        // 년도 검증
        try {
            int yearInt = Integer.parseInt(year);
            if (yearInt < 2020 || yearInt > 2030) {
                throw new IllegalArgumentException("올바르지 않은 년도입니다");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바르지 않은 년도 형식입니다");
        }

        // 월 검증
        try {
            int monthInt = Integer.parseInt(month);
            if (monthInt < 1 || monthInt > 12) {
                throw new IllegalArgumentException("올바르지 않은 월입니다");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바르지 않은 월 형식입니다");
        }
    }
}