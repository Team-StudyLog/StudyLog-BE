package org.example.studylog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.quiz.CreateQuizRequestDTO;
import org.example.studylog.dto.quiz.QuizResponseDTO;
import org.example.studylog.dto.studyrecord.CreateStudyRecordResponseDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.BusinessException;
import org.example.studylog.service.QuizService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

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
}
