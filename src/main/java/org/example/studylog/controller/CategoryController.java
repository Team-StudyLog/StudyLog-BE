package org.example.studylog.controller;

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
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createCategory(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
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

    @GetMapping
    public ResponseEntity<?> getCategories(
            @AuthenticationPrincipal CustomOAuth2User currentUser) {

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

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @PathVariable Long categoryId,
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