package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Streak", description = "스트릭(연속 학습) 관련 API")
public class StreakController {

    private final StreakService streakService;
    private final UserRepository userRepository;

    @Operation(
            summary = "월별 스트릭 조회",
            description = "특정 연도와 월의 일별 학습 기록 개수를 조회합니다. 인증된 사용자 또는 공유 코드로 조회 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "스트릭 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{"
                                            + "\"status\": 200,"
                                            + "\"message\": \"스트릭 조회에 성공하였습니다.\","
                                            + "\"data\": {"
                                            + "\"2025-07-01\": 0,"
                                            + "\"2025-07-02\": 3,"
                                            + "\"2025-07-03\": 1,"
                                            + "\"2025-07-04\": 0,"
                                            + "\"2025-07-05\": 2"
                                            + "}"
                                            + "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 년도/월)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{"
                                            + "\"status\": 400,"
                                            + "\"message\": \"잘못된 접근입니다\","
                                            + "\"data\": {"
                                            + "\"example\": \"올바르지 않은 월입니다\""
                                            + "}"
                                            + "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (접근 권한 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{"
                                            + "\"status\": 401,"
                                            + "\"message\": \"접근 권한이 없습니다.\","
                                            + "\"data\": false"
                                            + "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{"
                                            + "\"status\": 500,"
                                            + "\"message\": \"내부 서버 오류입니다. 다시 접속해주세요.\","
                                            + "\"data\": null"
                                            + "}"
                            )
                    )
            )
    })
    @GetMapping("/streak")
    public ResponseEntity<?> getMonthlyStreak(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User currentUser,
            @Parameter(description = "사용자 공유 코드 (선택적)", example = "ABC123") @RequestParam(required = false) String code,
            @Parameter(description = "조회할 년도", required = true, example = "2025") @RequestParam("year") String year,
            @Parameter(description = "조회할 월 (1-12)", required = true, example = "7") @RequestParam("month") String month) {

        // code 파라미터가 있으면 코드로 조회
        if (code != null && !code.trim().isEmpty()) {
            return getMonthlyStreakByCode(code, year, month);  // private 메서드 호출
        }

        // 기존 로직 (인증된 사용자)
        try {
            log.info("월별 스트릭 조회 요청: 사용자={}, year={}, month={}",
                    currentUser.getName(), year, month);

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "접근 권한이 없습니다.", false);
            }

            validateYearMonth(year, month);
            Object monthlyStreakData = streakService.getMonthlyStreakData(user, year, month);

            log.info("월별 스트릭 조회 성공: 사용자={}, year={}, month={}",
                    currentUser.getName(), year, month);

            return ResponseUtil.buildResponse(200, "스트릭 조회에 성공하였습니다.", monthlyStreakData);

        } catch (IllegalArgumentException e) {
            log.warn("월별 스트릭 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, "잘못된 접근입니다", Map.of("example", e.getMessage()));

        } catch (Exception e) {
            log.error("월별 스트릭 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다. 다시 접속해주세요.", null);
        }
    }

    // private 메서드 추가
    private ResponseEntity<?> getMonthlyStreakByCode(String code, String year, String month) {
        try {
            log.info("코드로 월별 스트릭 조회 요청: code={}, year={}, month={}", code, year, month);

            User user = userRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 코드입니다."));

            validateYearMonth(year, month);
            Object monthlyStreakData = streakService.getMonthlyStreakData(user, year, month);

            log.info("코드로 월별 스트릭 조회 성공: code={}, 사용자={}, year={}, month={}",
                    code, user.getOauthId(), year, month);

            return ResponseUtil.buildResponse(200, "스트릭 조회에 성공하였습니다.", monthlyStreakData);

        } catch (IllegalArgumentException e) {
            log.warn("코드로 월별 스트릭 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, "잘못된 접근입니다", Map.of("example", e.getMessage()));

        } catch (Exception e) {
            log.error("코드로 월별 스트릭 조회 중 오류 발생", e);
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