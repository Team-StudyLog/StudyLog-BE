package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.studyrecord.CreateStudyRecordRequestDTO;
import org.example.studylog.dto.studyrecord.CreateStudyRecordResponseDTO;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.StudyRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StudyRecordController {

    private final StudyRecordService studyRecordService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createStudyRecord(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @Valid @RequestBody CreateStudyRecordRequestDTO requestDTO) {

        try {
            log.info("기록 생성 요청: 사용자={}, 제목={}", currentUser.getName(), requestDTO.getTitle());

            // 현재 사용자 정보 조회
            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다");
            }

            // 기록 생성
            CreateStudyRecordResponseDTO responseDTO = studyRecordService.createStudyRecord(user, requestDTO);

            // 성공 응답
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("message", "학습 기록이 성공적으로 생성되었습니다");
            response.put("data", responseDTO);

            log.info("기록 생성 성공: 기록ID={}, 스트릭={}",
                    responseDTO.getRecord().getId(), responseDTO.getStreak().getCurrentStreak());

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("기록 생성 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("기록 생성 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다");
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("statusCode", status.value());
        errorResponse.put("message", message);
        errorResponse.put("data", null);

        return ResponseEntity.status(status).body(errorResponse);
    }
}