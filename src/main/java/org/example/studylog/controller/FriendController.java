package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.friend.FriendNameDTO;
import org.example.studylog.service.FriendService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
