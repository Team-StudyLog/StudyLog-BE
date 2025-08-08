package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.StudyRecordRepository;
import org.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private final StudyRecordRepository studyRecordRepository;
    private final UserRepository userRepository;



    @Transactional(readOnly = true)
    public Map<String, Integer> getMonthlyStreakData(User user, String year, String month) {
        log.info("월별 스트릭 데이터 조회 시작: 사용자={}, {}년 {}월", user.getOauthId(), year, month);

        Map<String, Integer> streakData = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // YearMonth 객체 생성
        int yearInt = Integer.parseInt(year);
        int monthInt = Integer.parseInt(month);
        YearMonth yearMonth = YearMonth.of(yearInt, monthInt);

        // 해당 월의 모든 날짜에 대해 기록 개수 조회
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(yearInt, monthInt, day);
            Long recordCount = studyRecordRepository.countByUserAndCreateDateDate(user, date);

            // 모든 날짜를 순서대로 LinkedHashMap에 추가
            streakData.put(date.format(formatter), recordCount.intValue());
        }

        log.info("월별 스트릭 데이터 조회 완료: 사용자={}, {}년 {}월, 총 일수={}",
                user.getOauthId(), year, month, streakData.size());

        return streakData;
    }
}