package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.ProfileResponseDTO;
import org.example.studylog.dto.friend.FriendNameDTO;
import org.example.studylog.dto.friend.FriendRequestDTO;
import org.example.studylog.dto.friend.FriendResponseDTO;
import org.example.studylog.service.FriendService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "code로 친구 조회", description = "친구 추가 시, code로 친구 조회하는 API")
    @ApiResponse(responseCode = "200", description = "사용자 이름 조회 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FriendNameDTO.class)))
    @GetMapping("by-code")
    public ResponseEntity<?> findUserByCode(@RequestParam String code) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        FriendNameDTO dto = friendService.findUserByCode(oauthId, code);
        return ResponseUtil.buildResponse(200, "사용자 이름 조회 완료", dto);
    }

    @Operation(summary = "친구 목록 조회", description = "로그인한 사용자의 친구 목록 조회 API")
    @ApiResponse(responseCode = "200", description = "친구 목록 조회 완료",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FriendResponseDTO.class))))
    @GetMapping
    public ResponseEntity<?> getFriendList(){
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        List<FriendResponseDTO> friends = friendService.getFriendList(oauthId);
        return ResponseUtil.buildResponse(200, "친구 목록 조회 완료", friends);
    }

    @Operation(summary = "친구 검색", description = "친구 목록에서 이름으로 친구 조회 API")
    @ApiResponse(responseCode = "200", description = "{query}에 대한 친구 검색 완료",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FriendResponseDTO.class))))
    @GetMapping("/search")
    public ResponseEntity<?> getFriendByQuery(@RequestParam String query){
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        List<FriendResponseDTO> friends = friendService.getFriendByQuery(oauthId, query);
        return ResponseUtil.buildResponse(200, String.format("\'%s\'에 대한 친구 검색 완료", query), friends);
    }

    @Operation(summary = "code로 친구 추가", description = "code로 친구 추가 API")
    @ApiResponse(responseCode = "201", description = "친구 추가 완료",
            content = @Content(
                    mediaType = "application/json"))
    @PostMapping
    public ResponseEntity<?> addFriend(@RequestBody @Valid FriendRequestDTO request) {
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        friendService.addFriend(request, oauthId);
        return ResponseUtil.buildResponse(201, "친구 추가 완료", null);
    }

    @Operation(summary = "친구 삭제", description = "friendId로 친구 삭제 API")
    @ApiResponse(responseCode = "200", description = "친구 삭제 완료",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FriendResponseDTO.class)))
    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long friendId){
        // 로그인한 사용자 oauthId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oauthId = auth.getName();

        FriendResponseDTO dto = friendService.deleteFriend(oauthId, friendId);
        return ResponseUtil.buildResponse(200, "친구 삭제 완료", dto);
    }

}

