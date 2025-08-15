package org.example.studylog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.category.CreateCategoryRequestDTO;
import org.example.studylog.dto.category.UpdateCategoryRequestDTO;
import org.example.studylog.dto.category.CategoryResponseDTO;
import org.example.studylog.dto.oauth.CustomOAuth2User;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.UserRepository;
import org.example.studylog.service.CategoryService;
import org.example.studylog.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Categories", description = "카테고리 관리 API")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\": 201, \"message\": \"카테고리가 성공적으로 생성되었습니다\", \"data\": {\"id\": 1, \"name\": \"Spring Boot\", \"color\": \"#FF5733\"}}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 카테고리명 등)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<?> createCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User currentUser,
            @Valid @RequestBody CreateCategoryRequestDTO requestDTO) {

        try {
            log.info("카테고리 생성 요청: 사용자={}, 이름={}", currentUser.getName(), requestDTO.getName());

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "유효하지 않은 사용자입니다", null);
            }

            CategoryResponseDTO responseDTO = categoryService.createCategory(user, requestDTO);

            log.info("카테고리 생성 성공: ID={}, 이름={}", responseDTO.getId(), responseDTO.getName());

            return ResponseUtil.buildResponse(201, "카테고리가 성공적으로 생성되었습니다", responseDTO);

        } catch (IllegalArgumentException e) {
            log.warn("카테고리 생성 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);

        } catch (Exception e) {
            log.error("카테고리 생성 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }

    @Operation(summary = "카테고리 목록 조회", description = "사용자의 모든 카테고리를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\": 200, \"message\": \"카테고리 목록 조회 성공\", \"data\": [{\"id\": 1, \"name\": \"Spring Boot\", \"color\": \"#FF5733\"}, {\"id\": 2, \"name\": \"React\", \"color\": \"#61DAFB\"}]}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<?> getCategories(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User currentUser) {

        try {
            log.info("카테고리 목록 조회: 사용자={}", currentUser.getName());

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "유효하지 않은 사용자입니다", null);
            }

            List<CategoryResponseDTO> categories = categoryService.getUserCategories(user);

            return ResponseUtil.buildResponse(200, "카테고리 목록 조회 성공", categories);

        } catch (Exception e) {
            log.error("카테고리 조회 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\": 200, \"message\": \"카테고리가 성공적으로 수정되었습니다\", \"data\": {\"id\": 1, \"name\": \"Spring Framework\", \"color\": \"#6DB33F\"}}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 카테고리명 등)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User currentUser,
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequestDTO requestDTO) {

        try {
            log.info("카테고리 수정 요청: 사용자={}, categoryId={}, 새이름={}",
                    currentUser.getName(), categoryId, requestDTO.getName());

            User user = userRepository.findByOauthId(currentUser.getName());
            if (user == null) {
                return ResponseUtil.buildResponse(401, "유효하지 않은 사용자입니다", null);
            }

            CategoryResponseDTO responseDTO = categoryService.updateCategory(user, categoryId, requestDTO);

            log.info("카테고리 수정 성공: ID={}, 새이름={}", responseDTO.getId(), responseDTO.getName());

            return ResponseUtil.buildResponse(200, "카테고리가 성공적으로 수정되었습니다", responseDTO);

        } catch (IllegalArgumentException e) {
            log.warn("카테고리 수정 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseUtil.buildResponse(400, e.getMessage(), null);

        } catch (Exception e) {
            log.error("카테고리 수정 중 오류 발생", e);
            return ResponseUtil.buildResponse(500, "내부 서버 오류입니다", null);
        }
    }
}