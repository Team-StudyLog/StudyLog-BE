package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.RankingResponseDTO;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.service.RankingService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "랭킹 조회", description = "현재 월의 기록순 랭킹을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "랭킹 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RankingResponseDTO.class)))
    })
    @GetMapping("/rankings")
    public ResponseEntity<?> getRanking(@AuthenticationPrincipal CustomOAuth2User currentUser,
                                        @RequestParam(required = false) Integer year,
                                        @RequestParam(required = false) Integer month) {
        log.info("랭킹 목록 조회 요청: 사용자={}", currentUser.getName());
        List<RankingResponseDTO> data = rankingService.getFriendRankings(currentUser.getName(), year, month);
        return ResponseUtil.buildResponse(200, "랭킹 목록 조회 완료", data);
    }

}
