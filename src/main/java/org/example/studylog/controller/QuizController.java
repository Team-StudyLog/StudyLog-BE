package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.BackgroundDTO;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.quiz.CreateQuizRequestDTO;
import org.example.studylog.dto.quiz.QuizListResponseDTO;
import org.example.studylog.dto.quiz.QuizResponseDTO;
import org.example.studylog.dto.studyrecord.CreateStudyRecordResponseDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.BusinessException;
import org.example.studylog.service.QuizService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "퀴즈 생성", description = "recordId로 친구 생성 API")
    @ApiResponse(responseCode = "200", description = "퀴즈 생성 완료",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = QuizResponseDTO.class))))
    @PostMapping("/{recordId}")
    public ResponseEntity<?> createQuiz(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @PathVariable Long recordId,
            @Valid @RequestBody CreateQuizRequestDTO requestDTO) {

        try {
            log.info("퀴즈 생성 요청: 사용자={}, 기록ID={}", currentUser.getName(), recordId);

            List<QuizResponseDTO> list = quizService.createQuiz(currentUser.getName(), recordId, requestDTO);
            log.info("퀴즈 생성 성공: 기록ID={}", recordId);

            return ResponseUtil.buildResponse(200, "퀴즈 생성 완료", list);

        } catch (BusinessException e) {
            log.warn("퀴즈 생성 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);

        } catch (Exception e) {
            log.error("퀴즈 생성 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }

    @Operation(summary = "퀴즈 상세 조회", description = "quizId로 퀴즈 상세 조회 API")
    @ApiResponse(responseCode = "200", description = "퀴즈 상세 조회 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = QuizResponseDTO.class)))
    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuiz(@AuthenticationPrincipal CustomOAuth2User currentUser,
                                     @PathVariable Long quizId){
        try {
            log.info("퀴즈 상세 조회: 사용자={}, 퀴즈ID={}", currentUser.getName(), quizId);

            QuizResponseDTO dto = quizService.getQuiz(currentUser.getName(), quizId);

            return ResponseUtil.buildResponse(200, "퀴즈 상세 조회 완료", dto);
        } catch (IllegalArgumentException e){
            log.warn("퀴즈 상세 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            log.error("퀴즈 생성 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }

    @Operation(summary = "퀴즈 목록 조회", description = "query, date, categoryId로 퀴즈 상세 조회 API")
    @ApiResponse(responseCode = "200", description = "퀴즈 목록 조회 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = QuizListResponseDTO.class)))
    @GetMapping
    public ResponseEntity<?> getQuizList(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    ) {
        QuizListResponseDTO quizzes = quizService.getQuizList(
                currentUser.getName(), query, lastId, size, date, categoryId
        );

        try{
            log.info("퀴즈 목록 조회: query = {}, date = {}, categoryId = {}, lastId = {}, szize = {}",
                    query, date, categoryId, lastId, size);

            return ResponseUtil.buildResponse(200, "퀴즈 목록 조회 완료", quizzes);

        } catch (IllegalArgumentException e){
            log.warn("퀴즈 목록 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            log.error("퀴즈 목록 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }

    }
}
