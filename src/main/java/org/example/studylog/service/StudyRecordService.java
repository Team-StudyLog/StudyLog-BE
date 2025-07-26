package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.CategoryDTO;
import org.example.studylog.dto.QuizDTO;
import org.example.studylog.dto.StreakDTO;
import org.example.studylog.dto.studyrecord.*;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.Streak;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.CategoryRepository;
import org.example.studylog.repository.StudyRecordRepository;
import org.example.studylog.repository.StreakRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordService {

    private final StudyRecordRepository studyRecordRepository;
    private final CategoryRepository categoryRepository;
    private final StreakRepository streakRepository;

    @Transactional
    public StudyRecordDTO updateStudyRecord(User user, Long recordId, UpdateStudyRecordRequestDTO requestDTO) {
        log.info("사용자 {}의 기록 수정 시작: recordId={}", user.getOauthId(), recordId);

        // 1. 기록 조회 및 권한 확인
        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다"));

        // 작성자 확인
        if (!studyRecord.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 기록을 수정할 권한이 없습니다");
        }

        // 2. 카테고리 검증
        Category category = categoryRepository.findByIdAndUser(requestDTO.getCategoryId(), user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다"));

        // 3. 기록 수정
        studyRecord.setTitle(requestDTO.getTitle());
        studyRecord.setContent(requestDTO.getContent());
        studyRecord.setCategory(category);

        StudyRecord updatedStudyRecord = studyRecordRepository.save(studyRecord);
        log.info("기록 수정 완료: ID={}", updatedStudyRecord.getId());

        return convertToStudyRecordDTO(updatedStudyRecord);
    }

    @Transactional
    public void deleteStudyRecord(User user, Long recordId) {
        log.info("사용자 {}의 기록 삭제 시작: recordId={}", user.getOauthId(), recordId);

        // 1. 기록 조회 및 권한 확인
        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다"));

        // 작성자 확인
        if (!studyRecord.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 기록을 삭제할 권한이 없습니다");
        }

        // 2. 기록 삭제 (연관된 퀴즈들도 CASCADE로 함께 삭제됨)
        studyRecordRepository.delete(studyRecord);
        log.info("기록 삭제 완료: ID={}", recordId);
    }

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

        StudyRecord savedStudyRecord = studyRecordRepository.save(studyRecord);
        log.info("기록 생성 완료: ID={}", savedStudyRecord.getId());

        // 3. 스트릭 업데이트
        StreakDTO streakDTO = updateUserStreak(user);

        // 4. 응답 DTO 생성
        StudyRecordDTO recordDTO = convertToStudyRecordDTO(savedStudyRecord);

        return CreateStudyRecordResponseDTO.builder()
                .record(recordDTO)
                .streak(streakDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public StudyRecordDetailResponseDTO getStudyRecordDetail(User user, Long recordId) {
        log.info("사용자 {}의 기록 상세 조회: recordId={}", user.getOauthId(), recordId);

        // N+1 방지: JOIN FETCH로 카테고리와 퀴즈 정보까지 한 번에 조회
        StudyRecord studyRecord = studyRecordRepository.findByIdWithCategoryAndQuizzes(recordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다"));

        // 작성자 확인
        if (!studyRecord.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 기록에 접근할 권한이 없습니다");
        }

        // 기록 DTO 변환
        StudyRecordDetailDTO recordDTO = convertToStudyRecordDetailDTO(studyRecord);

        // 퀴즈 목록 조회 (현재는 빈 리스트, 나중에 퀴즈 기능 구현 시 수정)
        List<QuizDTO> quizzes = getQuizzesForStudyRecord(studyRecord);

        return StudyRecordDetailResponseDTO.builder()
                .record(recordDTO)
                .quizzes(quizzes)
                .build();
    }

    @Transactional(readOnly = true)
    public StudyRecordFilterResponseDTO getStudyRecordsWithFilter(User user, StudyRecordFilterRequestDTO requestDTO) {
        log.info("사용자 {}의 기록 필터링 조회: categoryId={}, date={}, lastId={}",
                user.getOauthId(), requestDTO.getCategoryId(), requestDTO.getDate(), requestDTO.getLastId());

        // 페이지 크기 설정 (최대 20개로 제한)
        int pageSize = Math.min(requestDTO.getSize(), 20);
        Pageable pageable = PageRequest.of(0, pageSize + 1); // +1로 hasMore 판단

        List<StudyRecord> studyRecords;
        Category category = null;
        LocalDate filterDate = null;

        // 카테고리 검증 (제공된 경우)
        if (requestDTO.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUser(requestDTO.getCategoryId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다"));
        }

        // 날짜 파싱 (제공된 경우)
        if (requestDTO.getDate() != null && !requestDTO.getDate().trim().isEmpty()) {
            try {
                filterDate = LocalDate.parse(requestDTO.getDate());
            } catch (Exception e) {
                throw new IllegalArgumentException("올바르지 않은 날짜 형식입니다. YYYY-MM-DD 형식으로 입력해주세요");
            }
        }

        // 필터 조건에 따라 쿼리 실행
        if (category != null && filterDate != null) {
            // 카테고리 + 날짜 필터
            studyRecords = studyRecordRepository.findByUserAndCategoryAndDateWithPagination(
                    user, category, filterDate, requestDTO.getLastId(), pageable);
        } else if (category != null) {
            // 카테고리만 필터
            studyRecords = studyRecordRepository.findByUserAndCategoryWithPagination(
                    user, category, requestDTO.getLastId(), pageable);
        } else if (filterDate != null) {
            // 날짜만 필터
            studyRecords = studyRecordRepository.findByUserAndDateWithPagination(
                    user, filterDate, requestDTO.getLastId(), pageable);
        } else {
            // 필터 없음 (전체 조회)
            studyRecords = studyRecordRepository.findByUserWithPagination(
                    user, requestDTO.getLastId(), pageable);
        }

        // hasMore 판단 및 실제 반환할 데이터 추출
        boolean hasMore = studyRecords.size() > pageSize;
        List<StudyRecord> actualRecords = hasMore ?
                studyRecords.subList(0, pageSize) : studyRecords;

        // DTO 변환
        List<StudyRecordDTO> recordDTOs = actualRecords.stream()
                .map(this::convertToStudyRecordDTO)
                .collect(Collectors.toList());

        // 다음 lastId 계산
        Long nextLastId = null;
        if (hasMore && !actualRecords.isEmpty()) {
            nextLastId = actualRecords.get(actualRecords.size() - 1).getId();
        }

        log.info("필터링 조회 결과: {}건, hasMore={}", recordDTOs.size(), hasMore);

        return StudyRecordFilterResponseDTO.builder()
                .records(recordDTOs)
                .hasMore(hasMore)
                .nextLastId(nextLastId)
                .build();
    }

    @Transactional(readOnly = true)
    public StudyRecordListResponseDTO searchStudyRecordsByTitle(User user, String query) {
        log.info("사용자 {}의 기록 제목 검색: query={}", user.getOauthId(), query);

        // 쿼리가 비어있는지 확인
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 공백일 수 없습니다");
        }

        // 제목으로 기록 검색
        List<StudyRecord> studyRecords = studyRecordRepository
                .findByUserAndTitleContainingIgnoreCaseOrderByCreateDateDesc(user, query.trim());

        log.info("검색 결과: {}건", studyRecords.size());

        // DTO로 변환
        List<StudyRecordDTO> recordDTOs = studyRecords.stream()
                .map(this::convertToStudyRecordDTO)
                .collect(Collectors.toList());

        return StudyRecordListResponseDTO.builder()
                .records(recordDTOs)
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

    private StudyRecordDetailDTO convertToStudyRecordDetailDTO(StudyRecord studyRecord) {
        CategoryDTO categoryDTO = null;
        if (studyRecord.getCategory() != null) {
            String colorValue = studyRecord.getCategory().getColor().name().toLowerCase();

            categoryDTO = CategoryDTO.builder()
                    .id(studyRecord.getCategory().getId())
                    .name(studyRecord.getCategory().getName())
                    .color(colorValue)
                    .build();
        }

        // BaseEntity의 createDate 사용하여 날짜를 "2025-06-24" 형식으로 변환
        String formattedDate = studyRecord.getCreateDate().toLocalDate().toString();

        // 퀴즈 개수 계산 (현재는 0, 나중에 실제 퀴즈 개수로 변경)
        int quizCount = studyRecord.getQuizzes() != null ? studyRecord.getQuizzes().size() : 0;

        return StudyRecordDetailDTO.builder()
                .id(studyRecord.getId())
                .title(studyRecord.getTitle())
                .content(studyRecord.getContent())
                .category(categoryDTO)
                .createdAt(formattedDate)
                .quizCount(quizCount)
                .build();
    }

    private List<QuizDTO> getQuizzesForStudyRecord(StudyRecord studyRecord) {
        // 현재는 빈 리스트 반환 (퀴즈 기능 구현 시 실제 퀴즈 조회로 변경)
        // TODO: Quiz 엔티티와 QuizRepository 구현 후 실제 퀴즈 조회
        if (studyRecord.getQuizzes() == null || studyRecord.getQuizzes().isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }

        return studyRecord.getQuizzes().stream()
                .map(this::convertToQuizDTO)
                .toList();
    }

    private QuizDTO convertToQuizDTO(org.example.studylog.entity.quiz.Quiz quiz) {
        // Quiz 엔티티가 완전히 구현되면 실제 변환 로직 추가
        // 현재는 기본 구조만 제공
        String levelStr = convertQuizLevelToString(quiz.getLevel());
        String typeStr = convertQuizTypeToString(quiz.getType());

        return QuizDTO.builder()
                .id(quiz.getId())
                .question(quiz.getQuestion())
                .type(typeStr)
                .level(levelStr)
                .build();
    }

    private String convertQuizLevelToString(org.example.studylog.entity.quiz.QuizLevel level) {
        return switch (level) {
            case EASY -> "하";
            case MEDIUM -> "중";
            case HARD -> "상";
        };
    }

    private String convertQuizTypeToString(org.example.studylog.entity.quiz.QuizType type) {
        return switch (type) {
            case OX -> "OX";
            case SHORT_ANSWER -> "SHORT_ANSWER";
        };
    }
}