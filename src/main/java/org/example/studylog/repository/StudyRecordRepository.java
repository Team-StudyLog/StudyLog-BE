package org.example.studylog.repository;

import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    // 사용자의 기록을 최신순으로 조회
    List<StudyRecord> findByUserOrderByCreateDateDesc(User user);

    // 제목으로 기록 검색 (대소문자 구분 없음, 최신순 정렬)
    List<StudyRecord> findByUserAndTitleContainingIgnoreCaseOrderByCreateDateDesc(User user, String title);

    // 카테고리별 기록 조회 (무한 스크롤)
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user = :user AND sr.category = :category " +
            "AND (:lastId IS NULL OR sr.id < :lastId) " +
            "ORDER BY sr.id DESC")
    List<StudyRecord> findByUserAndCategoryWithPagination(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("lastId") Long lastId,
            Pageable pageable);

    // 날짜별 기록 조회 (무한 스크롤)
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user = :user " +
            "AND DATE(sr.createDate) = :date " +
            "AND (:lastId IS NULL OR sr.id < :lastId) " +
            "ORDER BY sr.id DESC")
    List<StudyRecord> findByUserAndDateWithPagination(
            @Param("user") User user,
            @Param("date") LocalDate date,
            @Param("lastId") Long lastId,
            Pageable pageable);

    // 카테고리 + 날짜별 기록 조회 (무한 스크롤)
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user = :user " +
            "AND sr.category = :category " +
            "AND DATE(sr.createDate) = :date " +
            "AND (:lastId IS NULL OR sr.id < :lastId) " +
            "ORDER BY sr.id DESC")
    List<StudyRecord> findByUserAndCategoryAndDateWithPagination(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("date") LocalDate date,
            @Param("lastId") Long lastId,
            Pageable pageable);

    // 전체 기록 조회 (무한 스크롤)
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user = :user " +
            "AND (:lastId IS NULL OR sr.id < :lastId) " +
            "ORDER BY sr.id DESC")
    List<StudyRecord> findByUserWithPagination(
            @Param("user") User user,
            @Param("lastId") Long lastId,
            Pageable pageable);

    // 특정 날짜에 사용자가 작성한 기록 개수 조회 (스트릭 계산용)
    @Query("SELECT COUNT(sr) FROM StudyRecord sr WHERE sr.user = :user AND DATE(sr.createDate) = :date")
    Long countByUserAndCreateDateDate(@Param("user") User user, @Param("date") LocalDate date);

    // 특정 날짜에 사용자가 작성한 기록들 조회
    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user = :user AND DATE(sr.createDate) = :date")
    List<StudyRecord> findByUserAndCreateDateDate(@Param("user") User user, @Param("date") LocalDate date);
}