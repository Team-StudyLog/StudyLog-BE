package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.CategoryDTO;
import org.example.studylog.dto.StreakDTO;
import org.example.studylog.dto.studyrecord.*;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.Streak;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.CategoryRepository;
import org.example.studylog.repository.StudyRecordRepository;
import org.example.studylog.repository.StreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordService {

    private final StudyRecordRepository studyRecordRepository;
    private final CategoryRepository categoryRepository;
    private final StreakRepository streakRepository;

    @Transactional
    public CreateStudyRecordResponseDTO createStudyRecord(User user, CreateStudyRecordRequestDTO requestDTO) {
        log.info("사용자 {}의 기록 생성 시작", user.getOauthId());

        // 1. 카테고리 검증 (필수)
        Category category = categoryRepository.findByIdAndUser(requestDTO.getCategoryId(), user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다"));

        // 2. 기록 생성
        StudyRecord studyRecord = StudyRecord.builder()
                .user(user)
                .category(category)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .isQuizCreated(false)  // 팀원 코드에 맞춰 isQuizCreated 사용
                .build();

        StudyRecord savedRecord = studyRecordRepository.save(studyRecord);
        log.info("기록 생성 완료: ID={}", savedRecord.getId());

        // 3. 스트릭 업데이트
        StreakDTO streakDTO = updateUserStreak(user);

        // 4. 응답 DTO 생성
        StudyRecordDTO recordDTO = convertToStudyRecordDTO(savedRecord);

        return CreateStudyRecordResponseDTO.builder()
                .record(recordDTO)
                .streak(streakDTO)
                .build();
    }

    @Transactional
    public StreakDTO updateUserStreak(User user) {
        LocalDate today = LocalDate.now();

        // 사용자의 스트릭 정보 조회 또는 생성
        Streak streak = streakRepository.findByUser(user)
                .orElse(Streak.builder()
                        .user(user)
                        .currentStreak(0)
                        .maxStreak(0)
                        .build());

        boolean isStreakUpdated = false;

        // 오늘 이미 기록을 작성했는지 확인
        if (streak.getLastRecordDate() == null || !streak.getLastRecordDate().equals(today)) {

            if (streak.getLastRecordDate() == null) {
                // 첫 기록
                streak.setCurrentStreak(1);
                isStreakUpdated = true;
            } else if (streak.getLastRecordDate().equals(today.minusDays(1))) {
                // 연속 기록
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
                isStreakUpdated = true;
            } else if (streak.getLastRecordDate().isBefore(today.minusDays(1))) {
                // 스트릭 끊김 - 새로 시작
                streak.setCurrentStreak(1);
                isStreakUpdated = true;
            }

            // 최대 스트릭 업데이트
            if (streak.getCurrentStreak() > streak.getMaxStreak()) {
                streak.setMaxStreak(streak.getCurrentStreak());
            }

            streak.setLastRecordDate(today);
            streakRepository.save(streak);

            log.info("스트릭 업데이트: 사용자={}, 현재스트릭={}, 업데이트여부={}",
                    user.getOauthId(), streak.getCurrentStreak(), isStreakUpdated);
        }

        return StreakDTO.builder()
                .currentStreak(streak.getCurrentStreak())
                .isStreakUpdated(isStreakUpdated)
                .build();
    }

    private StudyRecordDTO convertToStudyRecordDTO(StudyRecord studyRecord) {
        CategoryDTO categoryDTO = null;
        if (studyRecord.getCategory() != null) {
            // Color enum을 문자열로 변환 (예: BABY_BLUE -> "baby_blue")
            String colorValue = studyRecord.getCategory().getColor().name().toLowerCase();

            categoryDTO = CategoryDTO.builder()
                    .id(studyRecord.getCategory().getId())
                    .name(studyRecord.getCategory().getName())
                    .color(colorValue)
                    .build();
        }

        // BaseEntity의 createDate 사용하여 날짜를 "2025-06-24" 형식으로 변환
        String formattedDate = studyRecord.getCreateDate().toLocalDate().toString();

        return StudyRecordDTO.builder()
                .id(studyRecord.getId())
                .title(studyRecord.getTitle())
                .content(studyRecord.getContent())
                .category(categoryDTO)
                .createdAt(formattedDate)
                .hasQuiz(studyRecord.isQuizCreated())  // isQuizCreated -> hasQuiz로 변환
                .build();
    }
}