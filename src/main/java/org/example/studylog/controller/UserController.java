package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.*;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.dto.oauth.TokenDTO;
import org.example.studylog.service.UserService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "프로필 생성", description = "프로필 생성을 위한 api")
    @ApiResponse(
            responseCode = "200",
            description = "사용자 프로필 생성 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = ProfileResponseDTO.class
                    )))
    @PostMapping(path = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProfile(@Valid @ModelAttribute ProfileCreateRequestDTO request) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();
        log.info("사용자 프로필 생성 시작: oauthId = {}", oauthId);

        ProfileResponseDTO dto = userService.createUserProfile(request, oauthId);
        log.info("사용자 프로필 생성 완료: profileImage = {}, nickname = {}, intro = {}",
                dto.getProfileImage(), dto.getNickname(), dto.getIntro());
        return ResponseUtil.buildResponse(200, "사용자 프로필 생성 완료", dto);
    }

    @Operation(summary = "프로필 수정", description = "프로필 수정을 위한 api")
    @ApiResponse(responseCode = "200", description = "사용자 프로필 수정 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProfileResponseDTO.class)))
    @PatchMapping(path = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@ModelAttribute ProfileUpdateRequestDTO request) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        ProfileResponseDTO dto = userService.updateUserProfile(request, oauthId);
        return ResponseUtil.buildResponse(200, "사용자 프로필 수정 완료", dto);
    }

    @Operation(summary = "프로필 조회")
    @GetMapping("/profile")
    @ApiResponse(responseCode = "200", description = "사용자 프로필 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProfileResponseDTO.class)))
    public ResponseEntity<?> getProfile() {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        ProfileResponseDTO dto = userService.getUserProfile(oauthId);
        return ResponseUtil.buildResponse(200, "사용자 프로필 조회 성공", dto);
    }

    @Operation(summary = "로그인 유저의 마이페이지 조회")
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserInfoResponseDTO.class)))
    @GetMapping
    public ResponseEntity<?> getUserInfo() {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        UserInfoResponseDTO dto = userService.getUserInfo(oauthId);
        return ResponseUtil.buildResponse(200, "사용자 정보 조회 성공", dto);
    }

    @Operation(summary = "배경화면 수정")
    @PatchMapping(path = "/background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200", description = "사용자 배경화면 수정 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BackgroundDTO.ResponseDTO.class)))
    public ResponseEntity<?> updateBackground(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @Valid @ModelAttribute BackgroundDTO.RequestDTO dto) {
        try{
            log.info("배경화면 수정 요청: 사용자={}", currentUser.getName());

            BackgroundDTO.ResponseDTO responseDTO = userService.updateBackground(currentUser.getName(), dto);

            log.info("배경화면 수정 성공: 사용자={}, 배경화면={}", currentUser.getName(), responseDTO.getCoverImage());

            return ResponseUtil.buildResponse(200, "사용자 배경화면 수정 완료", responseDTO);
        } catch (IllegalStateException e){
            log.warn("배경화면 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e){
            log.error("배경화면 수정 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }

    @Operation(summary = "회원 탈퇴")
    @ApiResponse(responseCode = "204", description = "유저 삭제 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomOAuth2User currentUser){
        try {
            log.info("유저 삭제 요청: 사용자={}", currentUser.getName());
            String oauthId = currentUser.getName();
            userService.deleteAccount(oauthId);
            log.info("유저 삭제 완료: 사용자={}", currentUser.getName());

            return ResponseUtil.buildResponse(204, "유저 삭제 완료", null);
        } catch (IllegalStateException e){
            log.warn("유저 삭제 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e){
            log.error("유저 삭제 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }
}
