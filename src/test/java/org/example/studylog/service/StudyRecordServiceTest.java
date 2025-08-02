package org.example.studylog.service;

import org.example.studylog.dto.studyrecord.*;
import org.example.studylog.entity.Streak;
import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.category.Color;
import org.example.studylog.entity.user.Role;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.CategoryRepository;
import org.example.studylog.repository.StreakRepository;
import org.example.studylog.repository.StudyRecordRepository;
import org.example.studylog.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StudyRecordServiceTest {

    @Autowired
    private StudyRecordService studyRecordService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private StudyRecordRepository studyRecordRepository;
    @Autowired
    private StreakRepository streakRepository;

    // === 1. 데이터 무결성 테스트 ===

    @Test
    @DisplayName("기록 생성 중 카테고리가 삭제되는 경우")
    void createRecord_CategoryDeletedDuringCreation_ShouldFail() {
        // Given
        User user = createTestUser();
        Category category = createTestCategory(user);
        CreateStudyRecordRequestDTO requestDTO = createValidRequestDTO(category.getId());

        // 다른 스레드에서 카테고리 삭제 시뮬레이션
        CompletableFuture.runAsync(() -> {
            categoryRepository.delete(category);
            categoryRepository.flush();
        });

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> studyRecordService.createStudyRecord(user, requestDTO));
    }

    @Test
    @DisplayName("기록 생성 시 제목 길이 경계값 테스트")
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 19, 20, 21, 50})
    void createRecord_TitleLengthBoundary(int titleLength) {
        // Given
        User user = createTestUser();
        Category category = createTestCategory(user);

        CreateStudyRecordRequestDTO requestDTO = new CreateStudyRecordRequestDTO();
        requestDTO.setCategoryId(category.getId());
        requestDTO.setTitle("a".repeat(titleLength));
        requestDTO.setContent("유효한 내용입니다. 최소 10자 이상 작성");

        // When & Then
        if (titleLength == 0 || titleLength > 20) {
            assertThrows(Exception.class,
                    () -> studyRecordService.createStudyRecord(user, requestDTO));
        } else {
            assertDoesNotThrow(
                    () -> studyRecordService.createStudyRecord(user, requestDTO));
        }
    }

    @Test
    @DisplayName("기록 내용에 특수 문자 및 이모지 포함")
    void createRecord_SpecialCharactersAndEmojis() {
        // Given
        User user = createTestUser();
        Category category = createTestCategory(user);

        String specialContent = "특수문자 테스트 !@#$%^&*()_+ 이모지 테스트 😀🎉📚 " +
                "HTML 태그 <script>alert('xss')</script> " +
                "SQL 문자 '; DROP TABLE study_record; --";

        CreateStudyRecordRequestDTO requestDTO = new CreateStudyRecordRequestDTO();
        requestDTO.setCategoryId(category.getId());
        requestDTO.setTitle("특수문자 테스트");
        requestDTO.setContent(specialContent);

        // When
        CreateStudyRecordResponseDTO result = studyRecordService.createStudyRecord(user, requestDTO);

        // Then
        assertThat(result.getRecord().getContent()).contains("특수문자 테스트");
        // XSS 공격 문자열이 그대로 저장되지 않았는지 확인
        assertThat(result.getRecord().getContent()).doesNotContain("<script>");
    }

    // === 2. 동시성 테스트 ===

    @Test
    @DisplayName("동일 사용자가 동시에 여러 기록 생성")
    void createRecord_ConcurrentCreation_ShouldMaintainDataIntegrity() throws InterruptedException {
        // Given
        User user = createTestUser();
        Category category = createTestCategory(user);

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // When - 동시에 10개 기록 생성
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            CompletableFuture.runAsync(() -> {
                try {
                    CreateStudyRecordRequestDTO requestDTO = new CreateStudyRecordRequestDTO();
                    requestDTO.setCategoryId(category.getId());
                    requestDTO.setTitle("동시 생성 테스트 " + index);
                    requestDTO.setContent("동시 생성 테스트 내용입니다. " + index);

                    studyRecordService.createStudyRecord(user, requestDTO);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);

        // Then
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(exceptions).isEmpty();

        // 실제 DB에 모든 기록이 저장되었는지 확인
        List<StudyRecord> records = studyRecordRepository.findByUserOrderByCreateDateDesc(user);
        assertThat(records).hasSize(threadCount);

        // 스트릭이 정확히 계산되었는지 확인 (같은 날이므로 1이어야 함)
        Optional<Streak> streak = streakRepository.findByUser(user);
        assertThat(streak).isPresent();
        assertThat(streak.get().getCurrentStreak()).isEqualTo(1);
    }

    // === 3. 필터링 및 검색 테스트 ===

    @Test
    @DisplayName("페이징 경계값 테스트 - lastId가 Long.MAX_VALUE인 경우")
    void getRecordsWithFilter_MaxLastId_ShouldReturnEmpty() {
        // Given
        User user = createTestUser();
        createMultipleTestRecords(user, 20);

        StudyRecordFilterRequestDTO requestDTO = new StudyRecordFilterRequestDTO();
        requestDTO.setLastId(Long.MAX_VALUE);
        requestDTO.setSize(10);

        // When
        StudyRecordFilterResponseDTO result = studyRecordService.getStudyRecordsWithFilter(user, requestDTO);

        // Then
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getHasMore()).isFalse();
        assertThat(result.getNextLastId()).isNull();
    }

    @Test
    @DisplayName("잘못된 날짜 형식으로 필터링")
    @ParameterizedTest
    @ValueSource(strings = {"2024-13-01", "2024-02-30", "invalid-date", "2024/01/01", ""})
    void getRecordsWithFilter_InvalidDateFormats_ShouldThrowException(String invalidDate) {
        // Given
        User user = createTestUser();
        StudyRecordFilterRequestDTO requestDTO = new StudyRecordFilterRequestDTO();
        requestDTO.setDate(invalidDate);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> studyRecordService.getStudyRecordsWithFilter(user, requestDTO));
    }

    @Test
    @DisplayName("검색어에 SQL Injection 시도")
    void searchRecords_SqlInjectionAttempt_ShouldBeSafe() {
        // Given
        User user = createTestUser();
        createMultipleTestRecords(user, 5);

        String maliciousQuery = "'; DROP TABLE study_record; SELECT * FROM study_record WHERE '1'='1";

        // When
        StudyRecordListResponseDTO result = studyRecordService.searchStudyRecordsByTitle(user, maliciousQuery);

        // Then
        assertThat(result.getRecords()).isEmpty(); // 검색 결과는 없어야 함

        // 테이블이 여전히 존재하는지 확인
        long recordCount = studyRecordRepository.count();
        assertThat(recordCount).isGreaterThanOrEqualTo(5);
    }

    // === 4. 업데이트 및 삭제 테스트 ===

    @Test
    @DisplayName("기록 수정 중 카테고리 변경 - 존재하지 않는 카테고리로")
    void updateRecord_NonExistentCategory_ShouldFail() {
        // Given
        User user = createTestUser();
        Category originalCategory = createTestCategory(user);
        StudyRecord record = createTestRecord(user, originalCategory);

        UpdateStudyRecordRequestDTO requestDTO = new UpdateStudyRecordRequestDTO();
        requestDTO.setCategoryId(999L); // 존재하지 않는 카테고리
        requestDTO.setTitle("수정된 제목");
        requestDTO.setContent("수정된 내용입니다. 최소 10자 이상");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> studyRecordService.updateStudyRecord(user, record.getId(), requestDTO));
    }

    @Test
    @DisplayName("이미 삭제된 기록 수정 시도")
    void updateRecord_AlreadyDeletedRecord_ShouldFail() {
        // Given
        User user = createTestUser();
        Category category = createTestCategory(user);
        StudyRecord record = createTestRecord(user, category);
        Long recordId = record.getId();

        // 기록 삭제
        studyRecordService.deleteStudyRecord(user, recordId);

        UpdateStudyRecordRequestDTO requestDTO = createValidUpdateRequestDTO(category.getId());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> studyRecordService.updateStudyRecord(user, recordId, requestDTO));
    }

    // 테스트 데이터 생성 헬퍼 메서드들
    private User createTestUser() {
        User user = User.builder()
                .nickname("테스트유저")
                .profileImage("test.jpg")
                .intro("테스트 소개")
                .level(1)
                .role(Role.ROLE_USER)
                .isProfileCompleted(true)
                .uuid(UUID.randomUUID())
                .code("TEST1")
                .oauthId("test_oauth_id")
                .build();
        return userRepository.save(user);
    }

    private Category createTestCategory(User user) {
        Category category = Category.builder()
                .user(user)
                .name("테스트 카테고리")
                .color(Color.BABY_BLUE)
                .build();
        return categoryRepository.save(category);
    }
}