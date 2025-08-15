package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Main", description = "메인 페이지 API")
public class MainController {

    private final MainService mainService;
    private final UserRepository userRepository;

    @Operation(
            summary = "메인 페이지 조회",
            description = "메인 페이지 데이터를 조회합니다. 인증된 사용자 또는 공유 코드로 조회 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "메인 페이지 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{"
                                            + "\"status\": 200,"
                                            + "\"message\": \"메인 페이지 조회에 성공하였습니다.\","
                                            + "\"data\": {"
                                            + "\"following\": [],"
                                            + "\"profile\": {"
                                            + "\"userId\": 1,"
                                            + "\"coverImage\": null,"
                                            + "\"profileImage\": \"https://example.com/profile.jpg\","
                                            + "\"name\": \"홍길동\","
                                            + "\"intro\": \"안녕하세요! 개발 공부 중입니다.\","
                                            + "\"level\": 5,"
                                            + "\"code\": \"ABC123\""
                                            + "},"
                                            + "\"streak\": {"
                                            + "\"maxStreak\": 15,"
                                            + "\"recordCountPerDay\": {"
                                            + "\"2025-01-10\": 2,"
                                            + "\"2025-01-11\": 1,"
                                            + "\"2025-01-12\": 3"
                                            + "}"
                                            + "},"
                                            + "\"categories\": ["
                                            + "{\"name\": \"Spring Boot\", \"count\": 25},"
                                            + "{\"name\": \"React\", \"count\": 18}"
                                            + "],"
                                            + "\"isFollowing\": true"
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
                    responseCode = "404",
                    description = "사용자 코드를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{"
                                            + "\"status\": 404,"
                                            + "\"message\": \"존재하지 않는 사용자 코드입니다.\","
                                            + "\"data\": null"
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
    @GetMapping("/main")
    public ResponseEntity<?> getMainPage(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User currentUser,
            @Parameter(description = "사용자 공유 코드 (선택적)", example = "ABC123") @RequestParam(required = false) String code) {

        // code 파라미터가 있으면 코드로 조회, 없으면 기존 로직
        if (code != null && !code.trim().isEmpty()) {
            return getMainPageByCode(code, currentUser);  // private 메서드 호출
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
    private ResponseEntity<?> getMainPageByCode(String code, CustomOAuth2User currentUser) {
        try {
            log.info("코드로 메인 페이지 조회 요청: code={}", code);

            User targetUser = userRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 코드입니다."));

            // 현재 사용자가 있으면 팔로우 여부 확인
            User currentUserEntity = null;
            if (currentUser != null) {
                currentUserEntity = userRepository.findByOauthId(currentUser.getName());
            }

            Object mainPageData = mainService.getMainPageDataWithFollowStatus(targetUser, currentUserEntity);

            log.info("코드로 메인 페이지 조회 성공: code={}, 사용자={}", code, targetUser.getOauthId());

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