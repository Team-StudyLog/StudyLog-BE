package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.ProfileCreateRequestDTO;
import org.example.studylog.dto.ProfileResponseDTO;
import org.example.studylog.dto.UserInfoResponseDTO;
import org.example.studylog.service.UserService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "프로필 업데이트 api", description = "프로필 추가 및 수정을 위한 api")
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@ModelAttribute ProfileCreateRequestDTO request) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        ProfileResponseDTO dto = userService.createUserProfile(request, oauthId);
        return ResponseUtil.buildResponse(200, "사용자 프로필 생성 완료", dto);
    }

    @Operation(summary = "프로필 조회 api")
    @GetMapping("/profile")
    @ApiResponse(responseCode = "200", description = "성공 시 data 필드는 다음과 같습니다",
        content = @Content(schema = @Schema(implementation = ProfileResponseDTO.class)))
    public ResponseEntity<?> getProfile(){
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        ProfileResponseDTO dto = userService.getUserProfile(oauthId);
        return ResponseUtil.buildResponse(200, "사용자 프로필 조회 성공", dto);
    }

    @Operation(summary = "로그인 유저의 마이페이지 조회 api")
    @GetMapping
    public ResponseEntity<?> getUserInfo(){
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        UserInfoResponseDTO dto = userService.getUserInfo(oauthId);
        return ResponseUtil.buildResponse(200, "사용자 정보 조회 성공", dto);
    }


}
