package org.example.studylog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.ProfileRequestDTO;
import org.example.studylog.dto.ProfileResponseDTO;
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

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@ModelAttribute ProfileRequestDTO request) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        ProfileResponseDTO dto = userService.updateUserProfile(request, oauthId);
        return ResponseUtil.buildResponse(200, "사용자 프로필 업데이트 완료", dto);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        ProfileResponseDTO dto = userService.getUserProfile(oauthId);
        return ResponseUtil.buildResponse(200, "사용자 프로필 조회 성공", dto);
    }

}
