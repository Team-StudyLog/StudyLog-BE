package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.dto.MainPageResponseDTO;
import org.example.studylog.dto.friend.FriendResponseDTO;
import org.example.studylog.entity.Streak;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.CategoryRepository;
import org.example.studylog.repository.StreakRepository;
import org.example.studylog.repository.StudyRecordRepository;
import org.example.studylog.repository.custom.FriendRepositoryImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainService {

    private final FriendRepositoryImpl friendRepositoryImpl;
    private final StudyRecordRepository studyRecordRepository;
    private final StreakRepository streakRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public MainPageResponseDTO getMainPageData(User user) {
        log.info("메인 페이지 데이터 조회 시작: 사용자={}", user.getOauthId());

        // 1. 친구 목록 조회
        List<FriendResponseDTO> following = friendRepositoryImpl.findFriendListByUser(user);

        // 2. 프로필 정보 생성
        MainPageResponseDTO.ProfileDTO profile = MainPageResponseDTO.ProfileDTO.builder()
                .coverImage(user.getBackImage() != null ? user.getBackImage() : "https://example.com/bg.jpg")
                .profileImage(user.getProfileImage())
                .name(user.getNickname())
                .intro(user.getIntro())
                .level(user.getLevel())
                .uuid(user.getUuid().toString())
                .build();

        // 3. 스트릭 정보 생성 (현재 월 데이터 활용)
        Map<String, Integer> recordCountPerDay = getCurrentStreakData(user);
        Integer maxStreak = getMaxStreak(user);
        MainPageResponseDTO.StreakDTO streak = MainPageResponseDTO.StreakDTO.builder()
                .maxStreak(maxStreak)
                .recordCountPerDay(recordCountPerDay)
                .build();

        // 4. 카테고리별 기록 수 조회 (실제 데이터)
        List<MainPageResponseDTO.CategoryCountDTO> categories = getCategoryCountData(user);

        MainPageResponseDTO response = MainPageResponseDTO.builder()
                .following(following)
                .profile(profile)
                .streak(streak)
                .categories(categories)
                .build();

        log.info("메인 페이지 데이터 조회 완료: 친구수={}, 카테고리수={}",
                following.size(), categories.size());

        return response;
    }

    private Map<String, Integer> getCurrentStreakData(User user) {
        Map<String, Integer> streakData = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 현재 월의 모든 날짜에 대해 기록 수 조회
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), day);
            Long recordCount = studyRecordRepository.countByUserAndCreateDateDate(user, date);

            if (recordCount > 0) {
                streakData.put(date.format(formatter), recordCount.intValue());
            }
        }

        return streakData;
    }

    private Integer getMaxStreak(User user) {
        return streakRepository.findByUser(user)
                .map(Streak::getMaxStreak)
                .orElse(0);
    }

    private List<MainPageResponseDTO.CategoryCountDTO> getCategoryCountData(User user) {
        // N+1 쿼리 해결: 한 번의 쿼리로 카테고리별 기록 수 조회
        List<Object[]> categoryCountResults = studyRecordRepository.findCategoryCountsByUser(user);

        Map<Long, Integer> categoryCountMap = categoryCountResults.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],     // categoryId
                        result -> ((Long) result[1]).intValue()  // count
                ));

        // 카테고리 정보 조회
        List<Category> categories = categoryRepository.findByUserOrderByNameAsc(user);

        return categories.stream()
                .map(category -> MainPageResponseDTO.CategoryCountDTO.builder()
                        .name(category.getName())
                        .count(categoryCountMap.getOrDefault(category.getId(), 0))
                        .build())
                .toList();
    }
}