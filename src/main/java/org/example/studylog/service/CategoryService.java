package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.category.CreateCategoryRequestDTO;
import org.example.studylog.dto.category.UpdateCategoryRequestDTO;
import org.example.studylog.dto.category.CategoryResponseDTO;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDTO createCategory(User user, CreateCategoryRequestDTO requestDTO) {
        log.info("사용자 {}의 카테고리 생성 시작: {}", user.getOauthId(), requestDTO.getName());

        // 중복 이름 확인
        if (categoryRepository.existsByNameAndUser(requestDTO.getName(), user)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다");
        }

        // 카테고리 생성
        Category category = Category.builder()
                .user(user)
                .name(requestDTO.getName())
                .color(requestDTO.getColor())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 완료: ID={}, 이름={}", savedCategory.getId(), savedCategory.getName());

        return convertToCategoryResponseDTO(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(User user, Long categoryId, UpdateCategoryRequestDTO requestDTO) {
        log.info("사용자 {}의 카테고리 수정 시작: categoryId={}, 새이름={}",
                user.getOauthId(), categoryId, requestDTO.getName());

        // 카테고리 조회 및 권한 확인
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리이거나 수정 권한이 없습니다"));

        // 이름이 변경되는 경우에만 중복 체크
        if (!category.getName().equals(requestDTO.getName())) {
            if (categoryRepository.existsByNameAndUser(requestDTO.getName(), user)) {
                throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다");
            }
        }

        // 카테고리 수정
        category.setName(requestDTO.getName());
        category.setColor(requestDTO.getColor());

        Category updatedCategory = categoryRepository.save(category);
        log.info("카테고리 수정 완료: ID={}, 새이름={}", updatedCategory.getId(), updatedCategory.getName());

        return convertToCategoryResponseDTO(updatedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getUserCategories(User user) {
        log.info("사용자 {}의 카테고리 목록 조회", user.getOauthId());

        List<Category> categories = categoryRepository.findByUserOrderByNameAsc(user);

        return categories.stream()
                .map(this::convertToCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    private CategoryResponseDTO convertToCategoryResponseDTO(Category category) {
        // Color enum을 소문자로 변환
        String colorValue = category.getColor().name().toLowerCase();

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .color(colorValue)
                .build();
    }
}