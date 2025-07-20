package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.studyrecord.*;
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

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다");
            }

            CreateStudyRecordResponseDTO responseDTO = studyRecordService.createStudyRecord(user, requestDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 201);
            response.put("message", "학습 기록이 성공적으로 생성되었습니다");
            response.put("data", responseDTO);

            log.info("기록 생성 성공: 기록ID={}, 스트릭={}",
                    responseDTO.getRecord().getId(), responseDTO.getStreak().getCurrentStreak());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("기록 생성 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("기록 생성 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다");
        }
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> getStudyRecord(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @PathVariable Long recordId) {

        try {
            log.info("기록 상세 조회 요청: 사용자={}, recordId={}", currentUser.getName(), recordId);

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다");
            }

            StudyRecordDetailResponseDTO responseDTO = studyRecordService.getStudyRecordDetail(user, recordId);

            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("message", "기록 상세 정보를 성공적으로 조회했습니다");
            response.put("data", responseDTO);

            log.info("기록 상세 조회 성공: recordId={}", recordId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("기록 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("기록 조회 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다");
        }
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> updateStudyRecord(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @PathVariable Long recordId,
            @Valid @RequestBody UpdateStudyRecordRequestDTO requestDTO) {

        try {
            log.info("기록 수정 요청: 사용자={}, recordId={}, 제목={}",
                    currentUser.getName(), recordId, requestDTO.getTitle());

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다");
            }

            StudyRecordDTO responseDTO = studyRecordService.updateStudyRecord(user, recordId, requestDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("message", "기록이 성공적으로 수정되었습니다");
            response.put("data", responseDTO);

            log.info("기록 수정 성공: recordId={}", recordId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("기록 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("기록 수정 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다");
        }
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> deleteStudyRecord(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @PathVariable Long recordId) {

        try {
            log.info("기록 삭제 요청: 사용자={}, recordId={}", currentUser.getName(), recordId);

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다");
            }

            studyRecordService.deleteStudyRecord(user, recordId);

            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("message", "기록이 성공적으로 삭제되었습니다");
            response.put("data", null);

            log.info("기록 삭제 성공: recordId={}", recordId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("기록 삭제 실패 - 잘못된 요청: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("기록 삭제 중 오류 발생", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("=== TEST 엔드포인트 호출됨 ===");
        return ResponseEntity.ok("Controller 연결 성공!");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("statusCode", status.value());
        errorResponse.put("message", message);
        errorResponse.put("data", null);

        return ResponseEntity.status(status).body(errorResponse);
    }
}