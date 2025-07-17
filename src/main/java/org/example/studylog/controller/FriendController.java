package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.friend.FriendNameDTO;
import org.example.studylog.dto.friend.FriendRequestDTO;
import org.example.studylog.service.FriendService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "code로 친구 조회", description = "친구 추가 시, code로 친구 조회 API")
    @GetMapping(params = "code")
    public ResponseEntity<?> findUserByCode(@RequestParam String code) {
        FriendNameDTO dto = friendService.findUserByCode(code);
        return ResponseUtil.buildResponse(200, "사용자 이름 조회 완료", dto);
    }

    @Operation(summary = "code로 친구 추가", description = "코드로 친구 추가 API")
    @PostMapping
    public ResponseEntity<?> addFriend(@RequestBody @Valid FriendRequestDTO request) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        friendService.addFriend(request, oauthId);
        return ResponseUtil.buildResponse(201, "친구 추가 완료", null);
    }
}

