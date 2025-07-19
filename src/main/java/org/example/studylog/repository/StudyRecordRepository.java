package org.example.studylog.repository;

import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    // 사용자의 기록을 최신순으로 조회
    List<StudyRecord> findByUserOrderByCreateDateDesc(User user);

    // 특정 날짜에 사용자가 작성한 기록 개수 조회 (스트릭 계산용)
    @Query("SELECT COUNT(sr) FROM StudyRecord sr WHERE sr.user = :user AND DATE(sr.createDate) = :date")
    Long countByUserAndCreateDateDate(@Param("user") User user, @Param("date") LocalDate date);

    // 특정 날짜에 사용자가 작성한 기록들 조회
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user = :user AND DATE(sr.createDate) = :date")
    List<StudyRecord> findByUserAndCreateDateDate(@Param("user") User user, @Param("date") LocalDate date);
}