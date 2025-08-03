package org.example.studylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.entity.user.User;
import org.example.studylog.repository.StudyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private final StudyRecordRepository studyRecordRepository;

    @Transactional(readOnly = true)
    public Map<String, Integer> getMonthlyStreakData(User user, String year, String month) {
        log.info("월별 스트릭 데이터 조회 시작: 사용자={}, {}년 {}월", user.getOauthId(), year, month);

        Map<String, Integer> streakData = new HashMap<>();
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

            // 기록이 있는 날만 Map에 추가
//            if (recordCount > 0) {
//                streakData.put(date.format(formatter), recordCount.intValue());
//            }
            // 수정: 모든 날 반환
            streakData.put(date.format(formatter), recordCount.intValue());
        }

        log.info("월별 스트릭 데이터 조회 완료: 사용자={}, {}년 {}월, 기록 있는 날수={}",
                user.getOauthId(), year, month, streakData.size());

        return streakData;
    }
}