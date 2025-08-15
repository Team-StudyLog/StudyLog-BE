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
import org.example.studylog.repository.FriendRepository;
import org.example.studylog.repository.UserRepository;
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
    private final FriendRepository friendRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final StreakRepository streakRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MainPageResponseDTO getMainPageData(User user) {
        log.info("메인 페이지 데이터 조회 시작: 사용자={}", user.getOauthId());

        // 1. 친구 목록 조회
        List<FriendResponseDTO> following = friendRepositoryImpl.findFriendListByUser(user);

        // 2. 프로필 정보 생성
        MainPageResponseDTO.ProfileDTO profile = MainPageResponseDTO.ProfileDTO.builder()
                .userId(user.getId())
                .coverImage(user.getBackImage())  // null이면 null 반환
                .profileImage(user.getProfileImage())
                .name(user.getNickname())
                .intro(user.getIntro())
                .level(user.getLevel())
                .code(user.getCode())
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
                .isFollowing(null)  // 본인 페이지는 null
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
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))  // count 기준 내림차순 정렬
                .toList();
    }

    @Transactional(readOnly = true)
    public MainPageResponseDTO getMainPageDataWithFollowStatus(User targetUser, User currentUser) {
        log.info("메인 페이지 데이터 조회 (팔로우 확인): 대상={}, 현재 사용자={}", 
                targetUser.getOauthId(), currentUser != null ? currentUser.getOauthId() : "guest");

        // 기존 메인 페이지 데이터 조회
        List<FriendResponseDTO> following = friendRepositoryImpl.findFriendListByUser(targetUser);
        
        MainPageResponseDTO.ProfileDTO profile = MainPageResponseDTO.ProfileDTO.builder()
                .userId(targetUser.getId())
                .coverImage(targetUser.getBackImage())  // null이면 null 반환
                .profileImage(targetUser.getProfileImage())
                .name(targetUser.getNickname())
                .intro(targetUser.getIntro())
                .level(targetUser.getLevel())
                .code(targetUser.getCode())
                .build();
        
        Map<String, Integer> recordCountPerDay = getCurrentStreakData(targetUser);
        Integer maxStreak = getMaxStreak(targetUser);
        MainPageResponseDTO.StreakDTO streak = MainPageResponseDTO.StreakDTO.builder()
                .maxStreak(maxStreak)
                .recordCountPerDay(recordCountPerDay)
                .build();
        
        List<MainPageResponseDTO.CategoryCountDTO> categories = getCategoryCountData(targetUser);
        
        // 팔로우 여부 확인
        Boolean isFollowing = null;
        if (currentUser != null && !currentUser.getId().equals(targetUser.getId())) {
            isFollowing = friendRepository.existsByUserAndFriend(currentUser, targetUser);
        }
        
        MainPageResponseDTO response = MainPageResponseDTO.builder()
                .following(following)
                .profile(profile)
                .streak(streak)
                .categories(categories)
                .isFollowing(isFollowing)
                .build();

        log.info("메인 페이지 데이터 조회 완료: 친구수={}, 카테고리수={}, 팔로우여부={}",
                following.size(), categories.size(), isFollowing);

        return response;
    }

    @Transactional(readOnly = true)
    public MainPageResponseDTO getMainPageDataByCode(String code) {
        log.info("코드로 메인 페이지 데이터 조회 시작: code={}", code);

        // code로 사용자 조회
        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 코드입니다."));

        log.info("코드로 사용자 조회 완료: code={}, 사용자={}", code, user.getOauthId());

        // 기존 getMainPageData 메서드 로직 재사용
        MainPageResponseDTO response = getMainPageData(user);

        log.info("코드로 메인 페이지 데이터 조회 완료: code={}, 친구수={}, 카테고리수={}",
                code, response.getFollowing().size(), response.getCategories().size());

        return response;
    }
}